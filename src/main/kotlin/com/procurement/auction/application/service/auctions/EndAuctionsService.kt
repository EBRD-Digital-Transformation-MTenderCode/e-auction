package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatus
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.tender.snapshot.EndedAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.UrlGeneratorService
import com.procurement.auction.exception.app.DuplicateBidInBreakdownException
import com.procurement.auction.exception.app.DuplicateBidInResultsException
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.IncorrectNumberBidsInBreakdownException
import com.procurement.auction.exception.app.IncorrectNumberBidsInResultsException
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.app.UnknownBidInBreakdownException
import com.procurement.auction.exception.app.UnknownBidInResultException
import com.procurement.auction.exception.app.UnknownLotException
import com.procurement.auction.exception.command.CommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.dto.command.EndAuctionsCommand
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface EndAuctionsService {
    fun end(command: EndAuctionsCommand): EndedAuctionsSnapshot
}

@Service
class EndAuctionsServiceImpl(
    private val tenderRepository: TenderRepository,
    private val deserializer: JsonDeserializeService,
    private val urlGenerator: UrlGeneratorService
) : EndAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun end(command: EndAuctionsCommand): EndedAuctionsSnapshot {
        val cpid = command.context.cpid
        val ocid = command.context.ocid
        val entity = tenderRepository.loadEntity(cpid, ocid)
            ?: throw TenderNotFoundException(cpid, ocid)

        return when (entity.status) {
            AuctionsStatus.SCHEDULED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)

            AuctionsStatus.CANCELED ->
                throw CommandCanNotBeExecutedException(command.name, entity.status)

            AuctionsStatus.STARTED ->
                processing(command, entity.toStartedAuctionsSnapshot(deserializer))

            AuctionsStatus.ENDED -> {
                if (entity.operationId == command.context.operationId)
                    entity.toEndedAuctionsSnapshot(deserializer)
                else
                    throw CommandCanNotBeExecutedException(command.name, entity.status)
            }
        }
    }

    private fun processing(command: EndAuctionsCommand, snapshot: StartedAuctionsSnapshot): EndedAuctionsSnapshot {

        val startedAuctionsByLotId = snapshot.data.auctions.associateBy { it.lotId }

        validate(command, startedAuctionsByLotId)

        return endedAuctions(command, startedAuctionsByLotId, snapshot)
            .also {
                tenderRepository.save(it)
                log.info { "Ended auctions in tender with id: '${snapshot.data.tender.id}'." }
            }
    }

    private fun validate(command: EndAuctionsCommand,
                         startedAuctionsByLotId: Map<LotId, StartedAuctionsSnapshot.Data.Auction>) {
        val uniqueLotIds = mutableSetOf<LotId>()
        for (auction in command.data.tender.electronicAuctions.details) {
            val auctionId = auction.id
            val lotId = auction.relatedLot

            if (!uniqueLotIds.add(lotId))
                throw DuplicateLotException(lotId)

            val startedAuction = startedAuctionsByLotId[lotId]
                ?: throw UnknownLotException(lotId = lotId)
            val bidsStartedAuction = startedAuction.bids.associateBy { it.id }

            val countBidsInAuction = startedAuction.bids.size
            for (offer in auction.electronicAuctionProgress) {
                val offerId = offer.id

                if (countBidsInAuction != offer.breakdowns.size)
                    throw IncorrectNumberBidsInBreakdownException(
                        auctionId = auctionId,
                        lotId = lotId,
                        offerId = offerId
                    )

                val uniqueBidIds = mutableSetOf<BidId>()
                for (breakdown in offer.breakdowns) {
                    val bidId = breakdown.relatedBid

                    if (!uniqueBidIds.add(bidId))
                        throw DuplicateBidInBreakdownException(
                            auctionId = auctionId,
                            lotId = lotId,
                            offerId = offerId,
                            bidId = bidId
                        )

                    if (!bidsStartedAuction.containsKey(bidId))
                        throw UnknownBidInBreakdownException(
                            auctionId = auctionId,
                            lotId = lotId,
                            offerId = offerId,
                            bidId = bidId
                        )
                }
            }

            val uniqueBidIds = mutableSetOf<BidId>()
            for (result in auction.electronicAuctionResult) {
                val bidId = result.relatedBid

                if (!uniqueBidIds.add(bidId))
                    throw DuplicateBidInResultsException(
                        auctionId = auctionId,
                        lotId = lotId,
                        bidId = bidId
                    )

                if (!bidsStartedAuction.containsKey(bidId))
                    throw UnknownBidInResultException(
                        auctionId = auctionId,
                        lotId = lotId,
                        bidId = bidId
                    )
            }
            if (countBidsInAuction != uniqueBidIds.size)
                throw IncorrectNumberBidsInResultsException(auctionId = auctionId, lotId = lotId)
        }
    }

    private fun endedAuctions(command: EndAuctionsCommand,
                              startedAuctionsByLotId: Map<LotId, StartedAuctionsSnapshot.Data.Auction>,
                              snapshot: StartedAuctionsSnapshot): EndedAuctionsSnapshot {
        val cpid = snapshot.cpid
        val ocid = snapshot.ocid
        return EndedAuctionsSnapshot(
            rowVersion = snapshot.rowVersion.next(),
            operationId = command.context.operationId,
            cpid = cpid,
            ocid = ocid,
            data = EndedAuctionsSnapshot.Data(
                apiVersion = StartedAuctionsSnapshot.apiVersion,
                tender = EndedAuctionsSnapshot.Data.Tender(
                    id = snapshot.data.tender.id,
                    country = snapshot.data.tender.country,
                    status = AuctionsStatus.ENDED,
                    title = snapshot.data.tender.title,
                    description = snapshot.data.tender.description,
                    startDate = snapshot.data.tender.startDate,
                    endDate = command.data.tender.auctionPeriod.endDate
                ),
                slots = snapshot.data.slots.toSet(),
                auctions = command.data.tender.electronicAuctions.details.map { detail ->
                    val auction = startedAuctionsByLotId[detail.relatedLot]
                        ?: throw IllegalStateException("Unknown auction by lot with id: '${detail.relatedLot}'.")

                    EndedAuctionsSnapshot.Data.Auction(
                        id = auction.id,
                        lotId = auction.lotId,
                        title = auction.title,
                        description = auction.description,
                        auctionPeriod = detail.auctionPeriod.let { period ->
                            EndedAuctionsSnapshot.Data.Auction.AuctionPeriod(
                                startDate = period.startDate,
                                endDate = period.endDate
                            )
                        },
                        value = auction.value?.let { value ->
                            EndedAuctionsSnapshot.Data.Auction.Value(
                                amount = value.amount,
                                currency = value.currency
                            )
                        },
                        modalities = auction.modalities.map { modality ->
                            EndedAuctionsSnapshot.Data.Auction.Modality(
                                url = urlGenerator.forModality(ocid = ocid, relatedLot = auction.lotId),
                                eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                                    EndedAuctionsSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                        amount = emd.amount,
                                        currency = emd.currency
                                    )
                                }
                            )
                        },
                        bids = auction.bids.map { bid ->
                            EndedAuctionsSnapshot.Data.Auction.Bid(
                                id = bid.id,
                                owner = bid.owner,
                                relatedLot = bid.relatedLot,
                                pendingDate = bid.pendingDate,
                                value = bid.value.let { value ->
                                    EndedAuctionsSnapshot.Data.Auction.Bid.Value(
                                        amount = value.amount,
                                        currency = value.currency
                                    )
                                },
                                url = urlGenerator.forBid(ocid = ocid,
                                                          relatedLot = bid.relatedLot,
                                                          bidId = bid.id,
                                                          sign = bid.sign),
                                sign = bid.sign
                            )
                        },
                        progress = detail.electronicAuctionProgress.map { progress ->
                            EndedAuctionsSnapshot.Data.Auction.Offer(
                                id = progress.id,
                                period = progress.period.let { period ->
                                    EndedAuctionsSnapshot.Data.Auction.Offer.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                breakdowns = progress.breakdowns.map { breakdown ->
                                    EndedAuctionsSnapshot.Data.Auction.Offer.Breakdown(
                                        relatedBid = breakdown.relatedBid,
                                        status = BreakdownStatus("met"),
                                        dateMet = breakdown.dateMet,
                                        value = EndedAuctionsSnapshot.Data.Auction.Offer.Breakdown.Value(
                                            amount = breakdown.value.amount,
                                            currency = auction.value?.currency
                                        )
                                    )
                                }
                            )
                        },
                        results = detail.electronicAuctionResult.map { result ->
                            EndedAuctionsSnapshot.Data.Auction.Result(
                                relatedBid = result.relatedBid,
                                value = EndedAuctionsSnapshot.Data.Auction.Result.Value(
                                    amount = result.value.amount,
                                    currency = auction.value?.currency
                                )
                            )
                        }
                    )
                }
            )
        )
    }
}