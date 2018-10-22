package com.procurement.auction.converter

import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.request.auction.StartRQ
import com.procurement.auction.entity.auction.StartedAuctions
import com.procurement.auction.exception.LotInScheduledAuctionsNotFoundException
import com.procurement.auction.exception.ScheduledAuctionsNotFoundException
import com.procurement.auction.repository.ScheduledAuctionsRepository
import com.procurement.auction.service.SignService

interface StartedAuctionsConverter : Converter<StartRQ, StartedAuctions?>

class StartedAuctionsConverterImpl(private val scheduledAuctionsRepository: ScheduledAuctionsRepository,
                                   private val signService: SignService) : StartedAuctionsConverter {

    override fun convert(source: StartRQ): StartedAuctions? {
        return actualLots(source.data.bidsData)
            ?.let { actualLots ->
                auctions(source, actualLots)
                    ?.let { auctions ->
                        StartedAuctions(
                            tender = tender(source),
                            auctions = auctions,
                            bidders = bidders(source, auctions)
                        )
                    }
            }
    }

    private fun actualLots(bidsData: List<StartRQ.Data.BidData>): Set<RelatedLot>? {
        val lotsByBidders = mutableMapOf<RelatedLot, Int>()
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
            null
    }

    private fun tender(startRQ: StartRQ): StartedAuctions.Tender =
        startRQ.data.tender.let { tender ->
            StartedAuctions.Tender(
                cpid = tender.id,
                title = tender.title,
                description = tender.description
            )
        }

    private fun auctions(startRQ: StartRQ, actualLots: Set<RelatedLot>): StartedAuctions.Auctions? {
        val cpid = startRQ.context.cpid
        val scheduledAuctions = scheduledAuctionsRepository.loadLast(cpid)
            ?: throw ScheduledAuctionsNotFoundException("No scheduled auctions for cpid: '$cpid'.")

        val auctionsByRelatedLot =
            scheduledAuctions.electronicAuctions.details
                .associateBy { it.relatedLot }

        val tender = startRQ.data.tender
        val details: List<StartedAuctions.Auctions.Auction> = tender.lots.asSequence()
            .filter {
                actualLots.contains(it.id)
            }
            .map { lot ->
                val relatedLot = lot.id
                val scheduledAuction = auctionsByRelatedLot[relatedLot]
                    ?: throw LotInScheduledAuctionsNotFoundException("No scheduled auctions for cpid: '$cpid' with lot id: '$relatedLot'.")

                StartedAuctions.Auctions.Auction(
                    id = scheduledAuction.id,
                    title = lot.title,
                    description = lot.description,
                    relatedLot = relatedLot,
                    value = StartedAuctions.Auctions.Auction.Value(
                        amount = lot.value.amount,
                        currency = lot.value.currency
                    ),
                    auctionPeriod = StartedAuctions.Auctions.Auction.AuctionPeriod(
                        startDate = scheduledAuction.auctionPeriod.startDateTime
                    ),
                    electronicAuctionModalities = scheduledAuction.electronicAuctionModalities
                        .map { electronicAuctionModality ->
                            StartedAuctions.Auctions.Auction.ElectronicAuctionModality(
                                url = electronicAuctionModality.url,
                                eligibleMinimumDifference = StartedAuctions.Auctions.Auction.ElectronicAuctionModality.EligibleMinimumDifference(
                                    amount = electronicAuctionModality.eligibleMinimumDifference.amount,
                                    currency = electronicAuctionModality.eligibleMinimumDifference.currency
                                )
                            )
                        }
                )
            }.toList()

        return if (details.isNotEmpty())
            StartedAuctions.Auctions(details = details)
        else
            null
    }

    private fun bidders(startRQ: StartRQ, auctions: StartedAuctions.Auctions): StartedAuctions.Bidders {
        val auctionsByRelatedLot = auctions.details.associateBy { auction ->
            auction.relatedLot
        }
        val details = mutableListOf<StartedAuctions.Bidders.Bidder>()
        for (bidData in startRQ.data.bidsData) {
            val bids = bidData.bids.asSequence()
                .filter { bid ->
                    bid.relatedLots.isNotEmpty() && auctionsByRelatedLot.containsKey(bid.relatedLots[0])
                }
                .map { bid ->
                    val relatedLot = bid.relatedLots[0]
                    val bidId = bid.id
                    val sign = signService.sign()

                    StartedAuctions.Bidders.Bidder.Bid(
                        id = bidId,
                        relatedLot = relatedLot,
                        pendingDate = bid.pendingDate,
                        value = StartedAuctions.Bidders.Bidder.Bid.Value(
                            amount = bid.value.amount,
                            currency = bid.value.currency
                        ),
                        url = "${auctionsByRelatedLot[relatedLot]!!.electronicAuctionModalities[0].url}?bid_id=$bidId&sign=$sign",
                        sign = sign
                    )
                }.toList()

            if (bids.isNotEmpty()) {
                val bidder = StartedAuctions.Bidders.Bidder(
                    platformId = bidData.owner,
                    bids = bids
                )
                details.add(bidder)
            }
        }

        return StartedAuctions.Bidders(details = details)
    }
}