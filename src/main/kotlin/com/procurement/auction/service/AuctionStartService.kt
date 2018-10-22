package com.procurement.auction.service

import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.converter.AuctionConversionService
import com.procurement.auction.converter.convert
import com.procurement.auction.domain.request.auction.StartRQ
import com.procurement.auction.domain.response.auction.StartRS
import com.procurement.auction.entity.auction.StartedAuctions
import com.procurement.auction.repository.StartedAuctionsRepository
import org.springframework.stereotype.Service

interface AuctionStartService {
    fun start(startRQ: StartRQ): StartRS
}

@Service
class AuctionStartServiceImpl(
    private val startedAuctionsRepository: StartedAuctionsRepository,
    private val auctionConversionService: AuctionConversionService
) : AuctionStartService {

    override fun start(startRQ: StartRQ): StartRS {
        val startedAuctions: StartedAuctions? = getStartedAuctions(startRQ)

        return if (startedAuctions == null)
            StartRS(
                id = startRQ.id,
                version = GlobalProperties.AuctionStart.apiVersion,
                data = StartRS.Data(
                    isAuctionStarted = false
                )
            )
        else
            StartRS(
                id = startRQ.id,
                version = GlobalProperties.AuctionStart.apiVersion,
                data = StartRS.Data(
                    isAuctionStarted = true,
                    auctionsLinks = getAuctionsLinks(details = startedAuctions.bidders.details),
                    electronicAuctions = getElectronicAuctions(auctions = startedAuctions.auctions),
                    auctionsData = getAuctionsData(startedAuctions = startedAuctions)
                )
            )
    }

    private fun getStartedAuctions(startRQ: StartRQ): StartedAuctions? {
        val cpid = startRQ.context.cpid
        val operationId = startRQ.context.operationId

        val previousStartedAuctions = startedAuctionsRepository.load(cpid = cpid)
        if (previousStartedAuctions != null) return previousStartedAuctions

        val startedAuctions = auctionConversionService.convert<StartRQ, StartedAuctions?>(startRQ)
            ?: return null

        return startedAuctionsRepository.save(cpid = cpid, operationId = operationId, startedAuctions = startedAuctions)
    }

    private fun getAuctionsLinks(details: List<StartedAuctions.Bidders.Bidder>): List<StartRS.Data.AuctionsLink> =
        details.map { bidder ->
            getAuctionsLink(bidder)
        }

    private fun getAuctionsLink(bidder: StartedAuctions.Bidders.Bidder): StartRS.Data.AuctionsLink {
        val owner = bidder.platformId
        val links = bidder.bids.map { bid -> getLink(bid = bid) }
        return StartRS.Data.AuctionsLink(
            owner = owner,
            links = links
        )
    }

    private fun getLink(bid: StartedAuctions.Bidders.Bidder.Bid): StartRS.Data.AuctionsLink.Link =
        StartRS.Data.AuctionsLink.Link(
            relatedBid = bid.id,
            url = bid.url
        )

    private fun getElectronicAuctions(auctions: StartedAuctions.Auctions): StartRS.Data.ElectronicAuctions {
        return StartRS.Data.ElectronicAuctions(
            details = auctions.details.map { auction ->
                StartRS.Data.ElectronicAuctions.Detail(
                    id = auction.id,
                    relatedLot = auction.relatedLot,
                    auctionPeriod = StartRS.Data.ElectronicAuctions.Detail.AuctionPeriod(
                        startDate = auction.auctionPeriod.startDate
                    ),
                    electronicAuctionModalities = auction.electronicAuctionModalities.map { electronicAuctionModality ->
                        StartRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality(
                            url = electronicAuctionModality.url,
                            eligibleMinimumDifference = StartRS.Data.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                amount = electronicAuctionModality.eligibleMinimumDifference.amount,
                                currency = electronicAuctionModality.eligibleMinimumDifference.currency
                            )
                        )
                    }
                )
            }
        )
    }

    private fun getAuctionsData(startedAuctions: StartedAuctions): StartRS.Data.AuctionsData {
        val tender = startedAuctions.tender
        val auctions = startedAuctions.auctions
        return StartRS.Data.AuctionsData(
            tender = StartRS.Data.AuctionsData.Tender(
                id = tender.cpid,
                title = tender.title,
                description = tender.description,
                lots = auctions.details.map { auction ->
                    StartRS.Data.AuctionsData.Tender.Lot(
                        id = auction.relatedLot,
                        title = auction.title,
                        description = auction.description,
                        eligibleMinimumDifference = auction.electronicAuctionModalities[0].eligibleMinimumDifference.amount,
                        value = StartRS.Data.AuctionsData.Tender.Lot.Value(
                            amount = auction.value.amount,
                            currency = auction.value.currency
                        ),
                        auctionPeriod = StartRS.Data.AuctionsData.Tender.Lot.AuctionPeriod(
                            startDate = auction.auctionPeriod.startDate
                        )
                    )
                }
            ),
            bids = getBids(details = startedAuctions.bidders.details)
        )
    }

    private fun getBids(details: List<StartedAuctions.Bidders.Bidder>): List<StartRS.Data.AuctionsData.Bid> =
        mutableListOf<StartRS.Data.AuctionsData.Bid>().apply {
            for (bidder in details) {
                for (bid in bidder.bids) {
                    this.add(getBid(bid))
                }
            }
        }

    private fun getBid(bid: StartedAuctions.Bidders.Bidder.Bid): StartRS.Data.AuctionsData.Bid =
        StartRS.Data.AuctionsData.Bid(
            id = bid.id,
            value = bid.value.amount,
            relatedLot = bid.relatedLot,
            pendingDate = bid.pendingDate,
            sign = bid.sign
        )
}