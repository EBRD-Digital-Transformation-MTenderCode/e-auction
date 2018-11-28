package com.procurement.auction.application.service.auctions

import com.procurement.auction.domain.command.EndAuctionsCommand
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.EndedAuction
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.breakdown.Breakdown
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatus
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.offer.Offer
import com.procurement.auction.domain.model.period.Period
import com.procurement.auction.domain.model.result.Result
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.value.Value
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.exception.app.DuplicateBidInBreakdownException
import com.procurement.auction.exception.app.DuplicateBidInResultsException
import com.procurement.auction.exception.app.DuplicateLotException
import com.procurement.auction.exception.app.IncorrectNumberBidsInBreakdownException
import com.procurement.auction.exception.app.IncorrectNumberBidsInResultsException
import com.procurement.auction.exception.app.TenderNotFoundException
import com.procurement.auction.exception.app.UnknownBidInBreakdownException
import com.procurement.auction.exception.app.UnknownBidInResultException
import com.procurement.auction.exception.app.UnknownLotException
import com.procurement.auction.exception.command.EndCommandCanNotBeExecutedException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service

interface EndAuctionsService {
    fun end(command: EndAuctionsCommand): TenderSnapshot
}

@Service
class EndAuctionsServiceImpl(
    private val tenderRepository: TenderRepository
) : EndAuctionsService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    override fun end(command: EndAuctionsCommand): TenderSnapshot {
        val cpid = command.context.cpid
        val tender = tenderRepository.load(cpid)
            ?: throw TenderNotFoundException(cpid)

        if (!tender.canEnd) {
            if (tender.auctionsStatus == AuctionsStatus.ENDED && tender.operationId == command.context.operationId)
                return tender.toSnapshot()
            else
                throw EndCommandCanNotBeExecutedException("The '${command.name.code}' command cannot be executed. The tender is in '${tender.auctionsStatus.description}' status.")
        }

        validate(command, tender)

        val endedAuctions = endedAuctions(command, tender)
        tender.endAuctions(command.context.operationId,
                           command.data.tender.auctionPeriod.startDate,
                           command.data.tender.auctionPeriod.endDate,
                           endedAuctions
        )

        tenderRepository.saveEndedAuctions(cpid, tender)
        log.debug { "Ended auctions in tender with cpid: '$cpid'." }
        return tender.toSnapshot()
    }

    private fun validate(command: EndAuctionsCommand, tender: Tender) {
        val uniqueLotIds = mutableSetOf<LotId>()
        for (auction in command.data.tender.electronicAuctions.details) {
            val auctionId = auction.id
            val lotId = auction.relatedLot

            if (!uniqueLotIds.add(lotId))
                throw DuplicateLotException(lotId)

            val startedAuction = tender.startedAuctions[lotId]
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

    private fun endedAuctions(command: EndAuctionsCommand, tender: Tender): List<EndedAuction> {
        return command.data.tender.electronicAuctions.details.map { detail ->
            val lotId = detail.relatedLot
            val startedAuction = tender.startedAuctions[lotId]!!

            EndedAuction.of(
                startedAuction = startedAuction,
                period = detail.auctionPeriod.let { auctionPeriod ->
                    Period(
                        startDate = auctionPeriod.startDate,
                        endDate = auctionPeriod.endDate
                    )
                },
                progress = detail.electronicAuctionProgress.map { progress ->
                    Offer(
                        id = progress.id,
                        period = progress.period.let { period ->
                            Period(
                                startDate = period.startDate,
                                endDate = period.endDate
                            )
                        },
                        breakdowns = progress.breakdowns.map { breakdown ->
                            Breakdown(
                                relatedBid = breakdown.relatedBid,
                                status = BreakdownStatus("met"),
                                dateMet = breakdown.dateMet,
                                value = Value(
                                    amount = breakdown.value.amount,
                                    currency = startedAuction.value.currency
                                )

                            )
                        }
                    )
                },
                results = detail.electronicAuctionResult.map { result ->
                    Result(
                        relatedBid = result.relatedBid,
                        value = Value(
                            amount = result.value.amount,
                            currency = startedAuction.value.currency
                        )
                    )
                }
            )
        }
    }
}