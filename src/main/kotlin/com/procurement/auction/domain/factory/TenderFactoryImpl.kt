package com.procurement.auction.domain.factory

import com.procurement.auction.domain.model.auction.EndedAuction
import com.procurement.auction.domain.model.auction.ScheduledAuction
import com.procurement.auction.domain.model.auction.StartedAuction
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.Bid
import com.procurement.auction.domain.model.breakdown.Breakdown
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.modality.Modality
import com.procurement.auction.domain.model.offer.Offer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.period.Period
import com.procurement.auction.domain.model.result.Result
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.value.Value
import com.procurement.auction.domain.model.version.RowVersion
import org.springframework.stereotype.Service

@Service
class TenderFactoryImpl : TenderFactory {
    override fun create(rowVersion: RowVersion,
                        operationId: OperationId,
                        id: CPID,
                        country: Country,
                        auctionsStatus: AuctionsStatus,
                        data: TenderSnapshot.Data): Tender {

        return when (auctionsStatus) {
            AuctionsStatus.NONE -> throw IllegalStateException("Can not deserialize tender object in 'NONE' status.")

            AuctionsStatus.SCHEDULED -> Tender.ofSchedule(
                rowVersion = rowVersion,
                operationId = operationId,
                id = data.tender.id,
                country = country,
                startDate = data.tender.startDate!!,
                auctionsStatus = auctionsStatus,
                slots = data.slots,
                scheduledAuctions = data.auctions.map { auction ->
                    convertToScheduledAuction(auction)
                }
            )

            AuctionsStatus.CANCELED -> Tender.ofCancel(
                rowVersion = rowVersion,
                operationId = operationId,
                id = data.tender.id,
                country = country,
                auctionsStatus = auctionsStatus
            )

            AuctionsStatus.STARTED -> Tender.ofStarted(
                rowVersion = rowVersion,
                operationId = operationId,
                id = data.tender.id,
                country = country,
                title = data.tender.title!!,
                description = data.tender.description!!,
                startDate = data.tender.startDate!!,
                auctionsStatus = auctionsStatus,
                slots = data.slots,
                startedAuctions = data.auctions.map { auction ->
                    convertToStartedAuction(auction)
                }
            )

            AuctionsStatus.ENDED -> Tender.ofEnded(
                rowVersion = rowVersion,
                operationId = operationId,
                id = data.tender.id,
                country = country,
                title = data.tender.title!!,
                description = data.tender.description!!,
                startDate = data.tender.startDate!!,
                endDate = data.tender.endDate!!,
                auctionsStatus = auctionsStatus,
                slots = data.slots,
                endedAuctions = data.auctions.map { auction ->
                    convertToEndedAuction(auction)
                }
            )
        }
    }

    private fun convertToScheduledAuction(auction: TenderSnapshot.Data.Auction): ScheduledAuction {
        return ScheduledAuction.of(
            id = auction.id,
            lotId = auction.lotId,
            startDate = auction.auctionPeriod.startDate,
            modalities = auction.modalities.map { modality ->
                Modality(
                    url = modality.url,
                    eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                        Value(
                            amount = emd.amount,
                            currency = emd.currency
                        )
                    }
                )
            }
        )
    }

    private fun convertToStartedAuction(auction: TenderSnapshot.Data.Auction): StartedAuction {
        val scheduledAuction = convertToScheduledAuction(auction)

        return StartedAuction.of(
            scheduledAuction = scheduledAuction,
            title = auction.title!!,
            description = auction.description!!,
            value = auction.value!!.let { value ->
                Value(
                    amount = value.amount,
                    currency = value.currency
                )
            },
            bids = auction.bids!!.map { bid ->
                Bid(
                    id = bid.id,
                    owner = bid.owner,
                    relatedLot = bid.relatedLot,
                    value = bid.value.let { value ->
                        Value(
                            amount = value.amount,
                            currency = value.currency

                        )
                    },
                    pendingDate = bid.pendingDate,
                    url = bid.url,
                    sign = bid.sign
                )
            }
        )
    }

    private fun convertToEndedAuction(auction: TenderSnapshot.Data.Auction): EndedAuction {
        val startedAuction = convertToStartedAuction(auction)

        return EndedAuction.of(
            startedAuction = startedAuction,
            period = auction.auctionPeriod.let { period ->
                Period(
                    startDate = period.startDate,
                    endDate = period.endDate!!
                )
            },
            progress = auction.progress!!.map { offer ->
                Offer(
                    id = offer.id,
                    period = offer.period.let { period ->
                        Period(
                            startDate = period.startDate,
                            endDate = period.endDate
                        )
                    },
                    breakdowns = offer.breakdowns.map { breakdown ->
                        Breakdown(
                            relatedBid = breakdown.relatedBid,
                            status = breakdown.status,
                            dateMet = breakdown.dateMet,
                            value = breakdown.value.let { value ->
                                Value(
                                    amount = value.amount,
                                    currency = value.currency

                                )
                            }
                        )
                    }
                )
            },
            results = auction.results!!.map { result ->
                Result(
                    relatedBid = result.relatedBid,
                    value = result.value.let { value ->
                        Value(
                            amount = value.amount,
                            currency = value.currency

                        )
                    }
                )
            }
        )
    }
}