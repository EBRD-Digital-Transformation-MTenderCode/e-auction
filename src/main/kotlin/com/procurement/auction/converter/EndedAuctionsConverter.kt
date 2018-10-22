package com.procurement.auction.converter

import com.procurement.auction.domain.request.auction.EndRQ
import com.procurement.auction.entity.auction.EndedAuctions
import com.procurement.auction.exception.LotInStartedAuctionNotFoundException
import com.procurement.auction.exception.StartedAuctionNotFoundException
import com.procurement.auction.repository.StartedAuctionsRepository

interface EndedAuctionsConverter : Converter<EndRQ, EndedAuctions>

class EndedAuctionsConverterImpl(private val startedAuctionsRepository: StartedAuctionsRepository)
    : EndedAuctionsConverter {

    override fun convert(source: EndRQ): EndedAuctions {
        return EndedAuctions(
            tender = tender(source),
            auctions = auctions(source)
        )
    }

    private fun tender(endRQ: EndRQ): EndedAuctions.Tender =
        endRQ.data.tender.let { tender ->
            EndedAuctions.Tender(
                cpid = tender.id,
                auctionPeriod = EndedAuctions.Tender.AuctionPeriod(
                    startDate = tender.auctionPeriod.startDate,
                    endDate = tender.auctionPeriod.endDate
                )
            )
        }

    private fun auctions(endRQ: EndRQ): EndedAuctions.Auctions {
        val cpid = endRQ.context.cpid
        val startedAuctions = startedAuctionsRepository.loadAuctions(cpid)
            ?: throw StartedAuctionNotFoundException("No started auctions for cpid: '$cpid'.")

        val startedAuctionByRelatedLot = startedAuctions.details.associateBy { it.relatedLot }

        val details = endRQ.data.tender.electronicAuctions.details.map { auction ->
            val relatedLot = auction.relatedLot
            val startedAuction = startedAuctionByRelatedLot[relatedLot]
                ?: throw LotInStartedAuctionNotFoundException("No started auction for cpid: '$cpid' with lot id: '$relatedLot'.")
            val currency = startedAuction.value.currency

            EndedAuctions.Auctions.Detail(
                id = startedAuction.id,
                relatedLot = relatedLot,
                auctionPeriod = EndedAuctions.Auctions.Detail.AuctionPeriod(
                    startDate = auction.auctionPeriod.startDate,
                    endDate = auction.auctionPeriod.endDate
                ),
                electronicAuctionProgress = auction.electronicAuctionProgress.map { progress ->
                    EndedAuctions.Auctions.Detail.ElectronicAuctionProgress(
                        id = progress.id,
                        period = EndedAuctions.Auctions.Detail.ElectronicAuctionProgress.Period(
                            startDate = progress.period.startDate,
                            endDate = progress.period.endDate
                        ),
                        breakdowns = progress.breakdowns.map { breakdown ->
                            EndedAuctions.Auctions.Detail.ElectronicAuctionProgress.Breakdown(
                                relatedBid = breakdown.relatedBid,
                                dateMet = breakdown.dateMet,
                                value = EndedAuctions.Auctions.Detail.ElectronicAuctionProgress.Breakdown.Value(
                                    amount = breakdown.value.amount,
                                    currency = currency
                                )
                            )
                        }
                    )
                },
                electronicAuctionResult = auction.electronicAuctionResult.map { result ->
                    EndedAuctions.Auctions.Detail.ElectronicAuctionResult(
                        relatedBid = result.relatedBid,
                        value = EndedAuctions.Auctions.Detail.ElectronicAuctionResult.Value(
                            amount = result.value.amount,
                            currency = currency
                        )
                    )
                }
            )
        }

        return EndedAuctions.Auctions(details = details)
    }
}