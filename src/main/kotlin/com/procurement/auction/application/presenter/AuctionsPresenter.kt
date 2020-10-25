package com.procurement.auction.application.presenter

import com.procurement.auction.domain.model.platformId.PlatformId
import com.procurement.auction.domain.model.tender.snapshot.CancelledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.EndedAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot
import com.procurement.auction.domain.view.CancelledAuctionsView
import com.procurement.auction.domain.view.EndedAuctionsView
import com.procurement.auction.domain.view.ScheduledAuctionsView
import com.procurement.auction.domain.view.StartedAuctionsView
import org.springframework.stereotype.Service

interface AuctionsPresenter {
    fun presentScheduledAuctions(snapshot: ScheduledAuctionsSnapshot): ScheduledAuctionsView
    fun presentCancelledAuctions(snapshot: CancelledAuctionsSnapshot): CancelledAuctionsView
    fun presentNoStartedAuctions(): StartedAuctionsView
    fun presentStartedAuctions(snapshot: StartedAuctionsSnapshot): StartedAuctionsView
    fun presentEndedAuctions(snapshot: EndedAuctionsSnapshot): EndedAuctionsView
}

@Service
class AuctionsPresenterImpl : AuctionsPresenter {
    override fun presentScheduledAuctions(snapshot: ScheduledAuctionsSnapshot): ScheduledAuctionsView {
        return ScheduledAuctionsView(
            auctionPeriod = ScheduledAuctionsView.AuctionPeriod(
                startDate = snapshot.data.tender.startDate
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

    override fun presentCancelledAuctions(snapshot: CancelledAuctionsSnapshot): CancelledAuctionsView =
        CancelledAuctionsView()

    override fun presentNoStartedAuctions(): StartedAuctionsView {
        return StartedAuctionsView(isAuctionStarted = false)
    }

    override fun presentStartedAuctions(snapshot: StartedAuctionsSnapshot): StartedAuctionsView {
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
                    title = snapshot.data.tender.title,
                    description = snapshot.data.tender.description,
                    lots = snapshot.data.auctions.map { auction ->
                        StartedAuctionsView.AuctionsData.Tender.Lot(
                            id = auction.lotId,
                            title = auction.title,
                            description = auction.description,
                            eligibleMinimumDifference = auction.modalities[0].eligibleMinimumDifference.amount,
                            value = auction.value?.let { value ->
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
                            auction.bids.forEach { bid ->
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

    private fun links(snapshot: StartedAuctionsSnapshot): List<StartedAuctionsView.AuctionsLink> {
        val bidsByPlatformId = mutableMapOf<PlatformId, MutableSet<StartedAuctionsSnapshot.Data.Auction.Bid>>()
        snapshot.data.auctions.forEach { auction ->
            auction.bids.forEach { bid ->
                bidsByPlatformId.computeIfAbsent(bid.owner) {
                    mutableSetOf()
                }.apply {
                    this.add(bid)
                }
            }
        }

        return bidsByPlatformId.map { (platformId, bidsIds) ->
            StartedAuctionsView.AuctionsLink(
                owner = platformId,
                links = bidsIds.map {
                    StartedAuctionsView.AuctionsLink.Link(
                        relatedBid = it.id,
                        url = it.url
                    )
                }
            )
        }
    }

    override fun presentEndedAuctions(snapshot: EndedAuctionsSnapshot): EndedAuctionsView {
        return EndedAuctionsView(
            tender = EndedAuctionsView.Tender(
                auctionPeriod = EndedAuctionsView.Tender.AuctionPeriod(
                    startDate = snapshot.data.tender.startDate,
                    endDate = snapshot.data.tender.endDate
                ),
                electronicAuctions = EndedAuctionsView.Tender.ElectronicAuctions(
                    details = snapshot.data.auctions.map { auction ->
                        EndedAuctionsView.Tender.ElectronicAuctions.Detail(
                            id = auction.id,
                            relatedLot = auction.lotId,
                            auctionPeriod = auction.auctionPeriod.let { auctionPeriod ->
                                EndedAuctionsView.Tender.ElectronicAuctions.Detail.AuctionPeriod(
                                    startDate = auctionPeriod.startDate,
                                    endDate = auctionPeriod.endDate
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
                            electronicAuctionProgress = auction.progress.map { progress ->
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
                            electronicAuctionResult = auction.results.map { result ->
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