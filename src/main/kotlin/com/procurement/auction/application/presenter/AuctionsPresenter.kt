package com.procurement.auction.application.presenter

import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.platformId.PlatformId
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.view.CancelledAuctionsView
import com.procurement.auction.domain.view.EndedAuctionsView
import com.procurement.auction.domain.view.ScheduledAuctionsView
import com.procurement.auction.domain.view.StartedAuctionsView
import org.springframework.stereotype.Service

interface AuctionsPresenter {
    fun presentScheduledAuctions(snapshot: TenderSnapshot): ScheduledAuctionsView
    fun presentCancelledAuctions(snapshot: TenderSnapshot): CancelledAuctionsView
    fun presentNoStartedAuctions(): StartedAuctionsView
    fun presentStartedAuctions(snapshot: TenderSnapshot): StartedAuctionsView
    fun presentEndedAuctions(snapshot: TenderSnapshot): EndedAuctionsView
}

data class BidAndLotId(
    val lotId: LotId,
    val bid: TenderSnapshot.Data.Auction.Bid
)

@Service
class AuctionsPresenterImpl : AuctionsPresenter {
    override fun presentScheduledAuctions(snapshot: TenderSnapshot): ScheduledAuctionsView {
        return ScheduledAuctionsView(
            auctionPeriod = ScheduledAuctionsView.AuctionPeriod(
                startDate = snapshot.data.tender.startDate!!
            ),
            electronicAuctions = ScheduledAuctionsView.ElectronicAuctions(
                details = snapshot.data.auctions.map { auction ->
                    ScheduledAuctionsView.ElectronicAuctions.Detail(
                        id = auction.id,
                        relatedLot = auction.lotId,
                        auctionPeriod = auction.auctionPeriod.let { auctionPeriod ->
                            ScheduledAuctionsView.ElectronicAuctions.Detail.AuctionPeriod(
                                startDate = auctionPeriod.startDate
                            )
                        },
                        electronicAuctionModalities = auction.modalities.map { modality ->
                            ScheduledAuctionsView.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                url = modality.url,
                                eligibleMinimumDifference = modality.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                    ScheduledAuctionsView.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                        amount = eligibleMinimumDifference.amount,
                                        currency = eligibleMinimumDifference.currency
                                    )
                                }
                            )
                        }
                    )
                }
            )

        )
    }

    override fun presentCancelledAuctions(snapshot: TenderSnapshot): CancelledAuctionsView = CancelledAuctionsView()

    override fun presentNoStartedAuctions(): StartedAuctionsView {
        return StartedAuctionsView(isAuctionStarted = false)
    }

    override fun presentStartedAuctions(snapshot: TenderSnapshot): StartedAuctionsView {
        return StartedAuctionsView(
            isAuctionStarted = true,
            auctionsLinks = links(snapshot),
            electronicAuctions = snapshot.data.auctions.let { auctions ->
                StartedAuctionsView.ElectronicAuctions(
                    details = auctions.map { auction ->
                        StartedAuctionsView.ElectronicAuctions.Detail(
                            id = auction.id,
                            relatedLot = auction.lotId,
                            auctionPeriod = auction.auctionPeriod.let { auctionPeriod ->
                                StartedAuctionsView.ElectronicAuctions.Detail.AuctionPeriod(
                                    startDate = auctionPeriod.startDate
                                )
                            },
                            electronicAuctionModalities = auction.modalities.map { modality ->
                                StartedAuctionsView.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    url = modality.url,
                                    eligibleMinimumDifference = modality.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                        StartedAuctionsView.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                            amount = eligibleMinimumDifference.amount,
                                            currency = eligibleMinimumDifference.currency
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            },
            auctionsData = StartedAuctionsView.AuctionsData(
                tender = StartedAuctionsView.AuctionsData.Tender(
                    id = snapshot.data.tender.id,
                    title = snapshot.data.tender.title!!,
                    description = snapshot.data.tender.description!!,
                    lots = snapshot.data.auctions.map { auction ->
                        StartedAuctionsView.AuctionsData.Tender.Lot(
                            id = auction.lotId,
                            title = auction.title!!,
                            description = auction.description!!,
                            eligibleMinimumDifference = auction.modalities[0].eligibleMinimumDifference.amount,
                            value = auction.value!!.let { value ->
                                StartedAuctionsView.AuctionsData.Tender.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            auctionPeriod = StartedAuctionsView.AuctionsData.Tender.Lot.AuctionPeriod(
                                startDate = auction.auctionPeriod.startDate
                            )
                        )
                    }
                ),
                bids = snapshot.data.auctions.let { auctions ->
                    mutableListOf<StartedAuctionsView.AuctionsData.Bid>().apply {
                        for (auction in auctions) {
                            auction.bids!!.forEach { bid ->
                                add(
                                    StartedAuctionsView.AuctionsData.Bid(
                                        id = bid.id,
                                        value = bid.value.amount,
                                        relatedLot = bid.relatedLot,
                                        pendingDate = bid.pendingDate,
                                        sign = bid.sign
                                    )
                                )
                            }
                        }
                    }
                }
            )

        )
    }

    private fun links(snapshot: TenderSnapshot): List<StartedAuctionsView.AuctionsLink> {
        val bidsByPlatformId = mutableMapOf<PlatformId, MutableSet<BidAndLotId>>()
        snapshot.data.auctions.forEach { auction ->
            val lotId = auction.lotId

            auction.bids!!.forEach { bid ->
                val bidAndLotId = BidAndLotId(lotId = lotId, bid = bid)

                val setBidAndLotId = bidsByPlatformId[bid.owner]
                if (setBidAndLotId == null) {
                    bidsByPlatformId[bid.owner] = mutableSetOf<BidAndLotId>().apply {
                        add(bidAndLotId)
                    }
                } else {
                    setBidAndLotId.add(bidAndLotId)
                }
            }
        }

        return bidsByPlatformId.map { (platformId, bidAndLotIds) ->
            StartedAuctionsView.AuctionsLink(
                owner = platformId,
                links = bidAndLotIds.map {
                    StartedAuctionsView.AuctionsLink.Link(
                        relatedBid = it.bid.id,
                        url = it.bid.url
                    )
                }
            )
        }
    }

    override fun presentEndedAuctions(snapshot: TenderSnapshot): EndedAuctionsView {
        return EndedAuctionsView(
            tender = EndedAuctionsView.Tender(
                auctionPeriod = EndedAuctionsView.Tender.AuctionPeriod(
                    startDate = snapshot.data.tender.startDate!!,
                    endDate = snapshot.data.tender.endDate!!
                ),
                electronicAuctions = EndedAuctionsView.Tender.ElectronicAuctions(
                    details = snapshot.data.auctions.map { auction ->
                        EndedAuctionsView.Tender.ElectronicAuctions.Detail(
                            id = auction.id,
                            relatedLot = auction.lotId,
                            auctionPeriod = auction.auctionPeriod.let { auctionPeriod ->
                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.AuctionPeriod(
                                    startDate = auctionPeriod.startDate,
                                    endDate = auctionPeriod.endDate!!
                                )
                            },
                            electronicAuctionModalities = auction.modalities.map { modality ->
                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    url = modality.url,
                                    eligibleMinimumDifference = modality.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                        EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                            amount = eligibleMinimumDifference.amount,
                                            currency = eligibleMinimumDifference.currency
                                        )
                                    }
                                )
                            },
                            electronicAuctionProgress = auction.progress!!.map { progress ->
                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress(
                                    id = progress.id,
                                    period = progress.period.let { period ->
                                        EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                    breakdowns = progress.breakdowns.map { breakdown ->
                                        EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Breakdown(
                                            relatedBid = breakdown.relatedBid,
                                            status = breakdown.status,
                                            dateMet = breakdown.dateMet,
                                            value = breakdown.value.let { value ->
                                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionProgress.Breakdown.Value(
                                                    amount = value.amount,
                                                    currency = value.currency
                                                )
                                            }
                                        )
                                    }
                                )
                            },
                            electronicAuctionResult = auction.results!!.map { result ->
                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionResult(
                                    relatedBid = result.relatedBid,
                                    value = result.value.let { value ->
                                        EndedAuctionsView.Tender.ElectronicAuctions.Detail.ElectronicAuctionResult.Value(
                                            amount = value.amount,
                                            currency = value.currency
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            )
        )
    }
}