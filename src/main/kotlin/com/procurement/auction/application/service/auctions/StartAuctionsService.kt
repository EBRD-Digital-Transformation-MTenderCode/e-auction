package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.command.StartAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.StartedAuction
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.Bid
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.sign.Sign
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.value.Value
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.exception.app.BidIncorrectNumberLotsException
import com.procurement.auction.exception.app.BidOnUnknownLotException
import com.procurement.auction.exception.app.DuplicateBidException
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.NoBidsException
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.app.UnknownLotException
import com.procurement.auction.exception.app.ValidationException
import com.procurement.auction.exception.command.StartCommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger

import org.springframework.stereotype.Service

interface StartAuctionsService {
    fun start(command: StartAuctionsCommand): TenderSnapshot?
}

@Service
class StartAuctionsServiceImpl(
    private val tenderRepository: TenderRepository
) : StartAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun start(command: StartAuctionsCommand): TenderSnapshot? {
        val cpid = command.context.cpid
        val tender = tenderRepository.load(cpid)
            ?: throw TenderNotFoundException(cpid)

        if (!tender.canStart) {
            if (tender.auctionsStatus == AuctionsStatus.STARTED && tender.operationId == command.context.operationId)
                return tender.toSnapshot()
            else
                throw StartCommandCanNotBeExecutedException("The '${command.name.code}' command cannot be executed. The tender is in '${tender.auctionsStatus.description}' status.")
        }

        validate(command, tender)

        val actualLots = actualLots(command.data.bidsData)
        if (actualLots.isEmpty()) return null

        val bidsByLotId = bidsByLotId(command, actualLots, tender)
        val startedAuctions = startedAuctions(command, actualLots, bidsByLotId, tender)
        tender.startAuctions(operationId = command.context.operationId,
                             title = command.data.tender.title,
                             description = command.data.tender.description,
                             auctions = startedAuctions
        )

        tenderRepository.saveStartedAuctions(cpid, tender)
        log.debug { "Started auctions in tender with cpid: '$cpid'." }
        return tender.toSnapshot()
    }

    private fun validate(command: StartAuctionsCommand, tender: Tender) {
        if (command.data.tender.title.isBlank())
            throw ValidationException("tender.title", "empty")

        if (command.data.tender.description.isBlank())
            throw ValidationException("tender.description", "empty")

        val uniqueLotIds = mutableSetOf<LotId>()

        for (lot in command.data.tender.lots) {
            val lotId: LotId = lot.id
            if (!uniqueLotIds.add(lotId))
                throw DuplicateLotException(lotId)

            if (!tender.scheduledAuctions.containsKey(lotId))
                throw UnknownLotException(lotId = lotId)
        }

        val uniqueBidIds = mutableSetOf<BidId>()
        for (data in command.data.bidsData) {
            for (bid in data.bids) {
                val bidId = bid.id

                if (!uniqueBidIds.add(bidId))
                    throw DuplicateBidException(bidId)

                if (bid.relatedLots.size != 1)
                    throw BidIncorrectNumberLotsException(bidId)

                val relatedLot: LotId = bid.relatedLots[0]
                if (!uniqueLotIds.contains(relatedLot))
                    throw BidOnUnknownLotException(bidId = bidId, lotId = relatedLot)
            }
        }
        if (uniqueBidIds.isEmpty()) throw NoBidsException()
    }

    private fun actualLots(bidsData: List<StartAuctionsCommand.Data.BidData>): Set<LotId> {
        val lotsByBidders = mutableMapOf<LotId, Int>()
        for (bidData in bidsData) {
            for (bid in bidData.bids) {
                val relatedLots = bid.relatedLots
                if (relatedLots.isNotEmpty()) {
                    val relatedLot = relatedLots[0]
                    val item = lotsByBidders[relatedLot] ?: 0
                    lotsByBidders[relatedLot] = item + 1
                }
            }
        }

        return if (lotsByBidders.isNotEmpty()) {
            lotsByBidders.asSequence()
                .filter { it.value > 1 }
                .map { it.key }
                .toSet()
        } else
            emptySet()
    }

    private fun bidsByLotId(command: StartAuctionsCommand,
                            actualLots: Set<LotId>,
                            tender: Tender): Map<LotId, List<Bid>> {
        val result = mutableMapOf<LotId, MutableList<Bid>>()

        for (bidData in command.data.bidsData) {
            val owner = bidData.owner

            for (bid in bidData.bids) {
                if (bid.relatedLots.isNotEmpty() && actualLots.contains(bid.relatedLots[0])) {
                    val relatedLot = bid.relatedLots[0]
                    val bidId = bid.id
                    val sign = Sign()

                    val scheduledAuction = tender.scheduledAuctions[relatedLot]
                    if (scheduledAuction != null) {
                        val bidOfLot = Bid(
                            id = bidId,
                            owner = owner,
                            relatedLot = relatedLot,
                            pendingDate = bid.pendingDate,
                            value = bid.value.let { value ->
                                Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            url = "${scheduledAuction.modalities[0].url}?bid_id=${bidId.value}&sign=${sign.value}",
                            sign = sign
                        )

                        val listBid = result[relatedLot]
                        if (listBid == null) {
                            result[relatedLot] = mutableListOf(bidOfLot)
                        } else {
                            listBid.add(bidOfLot)
                        }
                    }
                }
            }
        }

        return result
    }

    private fun startedAuctions(command: StartAuctionsCommand,
                                actualLots: Set<LotId>,
                                bidsByLotId: Map<LotId, List<Bid>>,
                                tender: Tender): List<StartedAuction> {
        val result = mutableListOf<StartedAuction>()
        for (lot in command.data.tender.lots) {
            val lotId: LotId = lot.id
            if (actualLots.contains(lotId)) {
                val scheduledAuction = tender.scheduledAuctions[lotId]!!
                val bids = bidsByLotId[lotId]
                if (bids != null && bids.size > 1) {
                    val auction = StartedAuction.of(
                        scheduledAuction = scheduledAuction,
                        title = lot.title,
                        description = lot.description,
                        value = lot.value.let { value ->
                            Value(
                                amount = value.amount,
                                currency = value.currency
                            )
                        },
                        bids = bids
                    )
                    result.add(auction)
                }
            }
        }
        return result
    }
}