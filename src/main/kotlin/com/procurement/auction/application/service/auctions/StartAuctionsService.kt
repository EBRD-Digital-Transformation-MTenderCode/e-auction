package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.command.StartAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.sign.Sign
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.exception.app.BidIncorrectNumberLotsException
import com.procurement.auction.exception.app.BidOnUnknownLotException
import com.procurement.auction.exception.app.DuplicateBidException
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.NoBidsException
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.app.UnknownLotException
import com.procurement.auction.exception.app.ValidationException
import com.procurement.auction.exception.command.CommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface StartAuctionsService {
    fun start(command: StartAuctionsCommand): StartedAuctionsSnapshot?
}

@Service
class StartAuctionsServiceImpl(
    private val tenderRepository: TenderRepository,
    private val deserializer: JsonDeserializeService
) : StartAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun start(command: StartAuctionsCommand): StartedAuctionsSnapshot? {
        val cpid = command.context.cpid

        val entity = tenderRepository.loadEntity(cpid)
            ?: throw TenderNotFoundException(cpid)

        return when (entity.status) {
            AuctionsStatus.SCHEDULED -> processing(command, entity.toScheduledAuctionsSnapshot(deserializer))

            AuctionsStatus.CANCELED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)

            AuctionsStatus.STARTED -> {
                if (entity.operationId == command.context.operationId)
                    entity.toStartedAuctionsSnapshot(deserializer)
                else
                    throw CommandCanNotBeExecutedException(command.name, entity.status)
            }

            AuctionsStatus.ENDED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)
        }
    }

    private fun processing(command: StartAuctionsCommand,
                           snapshot: ScheduledAuctionsSnapshot): StartedAuctionsSnapshot? {
        val scheduledAuctions = snapshot.data.auctions.associateBy { it.lotId }

        validate(command, scheduledAuctions)

        val actualLots = actualLots(command.data.bidsData)
        if (actualLots.isEmpty()) return null

        return startedAuctions(command, actualLots, scheduledAuctions, snapshot)
            .also {
                tenderRepository.save(it)
                log.info { "Started auctions in tender with id: '${it.data.tender.id.value}'." }
            }
    }

    private fun validate(command: StartAuctionsCommand,
                         scheduledAuctions: Map<LotId, ScheduledAuctionsSnapshot.Data.Auction>) {
        if (command.data.tender.title.isBlank())
            throw ValidationException("tender.title", "empty")

        if (command.data.tender.description.isBlank())
            throw ValidationException("tender.description", "empty")

        val uniqueLotIds = mutableSetOf<LotId>()

        for (lot in command.data.tender.lots) {
            val lotId: LotId = lot.id
            if (!uniqueLotIds.add(lotId))
                throw DuplicateLotException(lotId)

            if (!scheduledAuctions.containsKey(lotId))
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

    private fun startedAuctions(command: StartAuctionsCommand,
                                actualLots: Set<LotId>,
                                scheduledAuctions: Map<LotId, ScheduledAuctionsSnapshot.Data.Auction>,
                                snapshot: ScheduledAuctionsSnapshot): StartedAuctionsSnapshot {

        val bidsByLotId: Map<LotId, List<StartedAuctionsSnapshot.Data.Auction.Bid>> =
            bidsByLotId(command, actualLots, scheduledAuctions)

        return StartedAuctionsSnapshot(
            rowVersion = snapshot.rowVersion.next(),
            operationId = command.context.operationId,
            data = StartedAuctionsSnapshot.Data(
                apiVersion = StartedAuctionsSnapshot.apiVersion,
                tender = StartedAuctionsSnapshot.Data.Tender(
                    id = snapshot.data.tender.id,
                    country = snapshot.data.tender.country,
                    status = AuctionsStatus.STARTED,
                    title = command.data.tender.title,
                    description = command.data.tender.description,
                    startDate = snapshot.data.tender.startDate
                ),
                slots = snapshot.data.slots.toSet(),
                auctions = mutableListOf<StartedAuctionsSnapshot.Data.Auction>().apply {
                    for (lot in command.data.tender.lots) {
                        val lotId: LotId = lot.id
                        if (actualLots.contains(lotId)) {
                            val scheduledAuction = scheduledAuctions[lotId]!!
                            val bids = bidsByLotId[lotId]
                            if (bids != null && bids.size > 1) {
                                val auction = StartedAuctionsSnapshot.Data.Auction(
                                    id = scheduledAuction.id,
                                    lotId = scheduledAuction.lotId,
                                    title = lot.title,
                                    description = lot.description,
                                    auctionPeriod = StartedAuctionsSnapshot.Data.Auction.AuctionPeriod(
                                        startDate = scheduledAuction.auctionPeriod.startDate
                                    ),
                                    value = lot.value.let { value ->
                                        StartedAuctionsSnapshot.Data.Auction.Value(
                                            amount = value.amount,
                                            currency = value.currency
                                        )
                                    },
                                    modalities = scheduledAuction.modalities.map { modality ->
                                        StartedAuctionsSnapshot.Data.Auction.Modality(
                                            url = modality.url,
                                            eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                                                StartedAuctionsSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                                    amount = emd.amount,
                                                    currency = emd.currency
                                                )
                                            }
                                        )
                                    },
                                    bids = bids.toList()
                                )
                                this.add(auction)
                            }
                        }
                    }
                }
            )
        )
    }

    private fun bidsByLotId(command: StartAuctionsCommand,
                            actualLots: Set<LotId>,
                            scheduledAuctions: Map<LotId, ScheduledAuctionsSnapshot.Data.Auction>): Map<LotId, List<StartedAuctionsSnapshot.Data.Auction.Bid>> {

        return mutableMapOf<LotId, MutableList<StartedAuctionsSnapshot.Data.Auction.Bid>>()
            .apply {
                for (bidData in command.data.bidsData) {
                    val owner = bidData.owner

                    for (bid in bidData.bids) {
                        if (bid.relatedLots.isNotEmpty() && actualLots.contains(bid.relatedLots[0])) {
                            val relatedLot = bid.relatedLots[0]
                            val bidId = bid.id
                            val sign = Sign()

                            val scheduledAuction = scheduledAuctions[relatedLot]
                            if (scheduledAuction != null) {
                                val bidOfLot = StartedAuctionsSnapshot.Data.Auction.Bid(
                                    id = bidId,
                                    owner = owner,
                                    relatedLot = relatedLot,
                                    pendingDate = bid.pendingDate,
                                    value = bid.value.let { value ->
                                        StartedAuctionsSnapshot.Data.Auction.Bid.Value(
                                            amount = value.amount,
                                            currency = value.currency
                                        )
                                    },
                                    url = "${scheduledAuction.modalities[0].url}?bid_id=${bidId.value}&sign=${sign.value}",
                                    sign = sign
                                )

                                val listBid = this[relatedLot]
                                if (listBid == null) {
                                    this[relatedLot] = mutableListOf(bidOfLot)
                                } else {
                                    listBid.add(bidOfLot)
                                }
                            }
                        }
                    }
                }

            }
    }
}