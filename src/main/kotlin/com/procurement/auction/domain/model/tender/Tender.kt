package com.procurement.auction.domain.model.tender

import com.procurement.auction.domain.model.auction.EndedAuction
import com.procurement.auction.domain.model.auction.ScheduledAuction
import com.procurement.auction.domain.model.auction.StartedAuction
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion
import java.time.LocalDateTime

class Tender private constructor(
    private var rowVersion: RowVersion,
    operationId: OperationId?,
    val id: CPID,
    val country: Country,
    title: String?,
    description: String?,
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
    auctionsStatus: AuctionsStatus,
    slots: Set<SlotId>,
    scheduledAuctions: Map<LotId, ScheduledAuction>,
    startedAuctions: Map<LotId, StartedAuction>,
    endedAuctions: Map<LotId, EndedAuction>
) {
    companion object {
        val apiVersion = ApiVersion(0, 0, 1)

        fun of(cpid: CPID, country: Country): Tender {
            return Tender(
                rowVersion = RowVersion.of(),
                operationId = null,
                id = cpid,
                country = country,
                title = null,
                description = null,
                startDate = null,
                endDate = null,
                auctionsStatus = AuctionsStatus.NONE,
                slots = emptySet(),
                scheduledAuctions = emptyMap(),
                startedAuctions = emptyMap(),
                endedAuctions = emptyMap()
            )
        }

        fun ofSchedule(rowVersion: RowVersion,
                       operationId: OperationId,
                       id: CPID,
                       country: Country,
                       startDate: LocalDateTime,
                       auctionsStatus: AuctionsStatus,
                       slots: Set<SlotId>,
                       scheduledAuctions: List<ScheduledAuction>): Tender {
            if (rowVersion.isNew)
                throw IllegalStateException("The attempt to create a tender with invalid row version.")

            if (auctionsStatus != AuctionsStatus.SCHEDULED)
                throw IllegalStateException("The attempt to create a tender with scheduled auctions with invalid auction status '$auctionsStatus'.")

            return Tender(
                rowVersion = rowVersion,
                operationId = operationId,
                id = id,
                country = country,
                title = null,
                description = null,
                startDate = startDate,
                endDate = null,
                auctionsStatus = auctionsStatus,
                slots = slots.toSet(),
                scheduledAuctions = scheduledAuctions.associateBy { it.lotId },
                startedAuctions = emptyMap(),
                endedAuctions = emptyMap()
            )
        }

        fun ofCancel(rowVersion: RowVersion,
                     operationId: OperationId,
                     id: CPID,
                     country: Country,
                     auctionsStatus: AuctionsStatus
        ): Tender {
            if (rowVersion.isNew)
                throw IllegalStateException("The attempt to create a tender with invalid row version.")

            if (auctionsStatus != AuctionsStatus.CANCELED)
                throw IllegalStateException("The attempt to create a tender with cancelled auctions with invalid auction status '$auctionsStatus'.")

            return Tender(
                rowVersion = rowVersion,
                operationId = operationId,
                id = id,
                country = country,
                title = null,
                description = null,
                startDate = null,
                endDate = null,
                auctionsStatus = auctionsStatus,
                slots = emptySet(),
                scheduledAuctions = emptyMap(),
                startedAuctions = emptyMap(),
                endedAuctions = emptyMap()
            )
        }

        fun ofStarted(rowVersion: RowVersion,
                      operationId: OperationId,
                      id: CPID,
                      country: Country,
                      title: String,
                      description: String,
                      startDate: LocalDateTime,
                      auctionsStatus: AuctionsStatus,
                      slots: Set<SlotId>,
                      startedAuctions: List<StartedAuction>): Tender {
            if (rowVersion.isNew)
                throw IllegalStateException("The attempt to create a tender with invalid row version.")

            if (auctionsStatus != AuctionsStatus.STARTED)
                throw IllegalStateException("The attempt to create a tender with started auctions with invalid auction status '$auctionsStatus'.")

            return Tender(
                rowVersion = rowVersion,
                operationId = operationId,
                id = id,
                country = country,
                title = title,
                description = description,
                startDate = startDate,
                endDate = null,
                auctionsStatus = auctionsStatus,
                slots = slots.toSet(),
                scheduledAuctions = emptyMap(),
                startedAuctions = startedAuctions.associateBy { it.lotId },
                endedAuctions = emptyMap()
            )
        }

        fun ofEnded(rowVersion: RowVersion,
                    id: CPID,
                    operationId: OperationId,
                    country: Country,
                    title: String?,
                    description: String?,
                    startDate: LocalDateTime,
                    endDate: LocalDateTime,
                    auctionsStatus: AuctionsStatus,
                    slots: Set<SlotId>,
                    endedAuctions: List<EndedAuction>): Tender {
            if (rowVersion.isNew)
                throw IllegalStateException("The attempt to create a tender with invalid row version.")

            if (auctionsStatus != AuctionsStatus.ENDED)
                throw IllegalStateException("The attempt to create a tender with ended auctions with invalid auction status '$auctionsStatus'.")

            return Tender(
                rowVersion = rowVersion,
                operationId = operationId,
                id = id,
                country = country,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                auctionsStatus = auctionsStatus,
                slots = slots.toSet(),
                scheduledAuctions = emptyMap(),
                startedAuctions = emptyMap(),
                endedAuctions = endedAuctions.associateBy { it.lotId }
            )
        }
    }

    var auctionsStatus: AuctionsStatus = auctionsStatus
        private set(value) {
            field = value
        }

    var operationId: OperationId? = operationId
        private set(value) {
            field = value
        }

    var title: String? = title
        private set(value) {
            field = value
        }

    var description: String? = description
        private set(value) {
            field = value
        }

    var startDate: LocalDateTime? = startDate
        private set(value) {
            field = value
        }

    var endDate: LocalDateTime? = endDate
        private set(value) {
            field = value
        }

    var slots: Set<SlotId> = slots
        private set(value) {
            field = value
        }

    var scheduledAuctions: Map<LotId, ScheduledAuction> = scheduledAuctions
        private set(value) {
            field = value
        }

    var startedAuctions: Map<LotId, StartedAuction> = startedAuctions
        private set(value) {
            field = value
        }

    var endedAuctions: Map<LotId, EndedAuction> = endedAuctions
        private set(value) {
            field = value
        }

    val canSchedule: Boolean
        get() = auctionsStatus == AuctionsStatus.NONE || auctionsStatus == AuctionsStatus.CANCELED
    val canCancel: Boolean
        get() = auctionsStatus == AuctionsStatus.SCHEDULED
    val canStart: Boolean
        get() = auctionsStatus == AuctionsStatus.SCHEDULED
    val canEnd: Boolean
        get() = auctionsStatus == AuctionsStatus.STARTED

    fun scheduleAuctions(operationId: OperationId,
                         startDate: LocalDateTime,
                         auctions: List<ScheduledAuction>,
                         slots: Set<SlotId>) {
        if (!canSchedule)
            throw IllegalStateException("Auctions cannot be schedule. Current auctions status: '$auctionsStatus'")

        rowVersion = rowVersion.next()
        this.operationId = operationId
        this.startDate = startDate
        auctionsStatus = AuctionsStatus.SCHEDULED
        this.slots = slots
        scheduledAuctions = auctions.associateBy { it.lotId }
    }

    fun cancelAuctions(operationId: OperationId) {
        if (!canCancel)
            throw IllegalStateException("Auctions cannot be cancel. Current auctions status: '$auctionsStatus'")

        rowVersion = rowVersion.next()
        this.operationId = operationId
        auctionsStatus = AuctionsStatus.CANCELED
        slots = emptySet()
        scheduledAuctions = emptyMap()
        startedAuctions = emptyMap()
        endedAuctions = emptyMap()
    }

    fun startAuctions(operationId: OperationId, title: String, description: String, auctions: List<StartedAuction>) {
        if (!canStart)
            throw IllegalStateException("Auctions cannot be start. Current auctions status: '$auctionsStatus'")

        rowVersion = rowVersion.next()
        this.operationId = operationId
        this.title = title
        this.description = description
        auctionsStatus = AuctionsStatus.STARTED
        scheduledAuctions = emptyMap()
        startedAuctions = auctions.associateBy { it.lotId }
    }

    fun endAuctions(operationId: OperationId,
                    startDate: LocalDateTime,
                    endDate: LocalDateTime,
                    auctions: List<EndedAuction>) {
        if (!canEnd)
            throw IllegalStateException("Auctions cannot be end. Current auctions status: '$auctionsStatus'")

        rowVersion = rowVersion.next()
        this.operationId = operationId
        this.startDate = startDate
        this.endDate = endDate
        auctionsStatus = AuctionsStatus.ENDED
        scheduledAuctions = emptyMap()
        startedAuctions = emptyMap()
        endedAuctions = auctions.associateBy { it.lotId }
    }

    fun toSnapshot(): TenderSnapshot {
        val auctionsSnapshot: List<TenderSnapshot.Data.Auction> = when (auctionsStatus) {
            AuctionsStatus.NONE -> throw IllegalStateException("Can not serialize tender object in 'NONE' status.")
            AuctionsStatus.SCHEDULED -> scheduledAuctionsSnapshot()
            AuctionsStatus.CANCELED -> emptyList()
            AuctionsStatus.STARTED -> startedAuctionsSnapshot()
            AuctionsStatus.ENDED -> endedAuctionsSnapshot()
        }

        return TenderSnapshot(
            rowVersion = rowVersion,
            operationId = operationId!!,
            country = country,
            data = TenderSnapshot.Data(
                apiVersion = apiVersion,
                tender = TenderSnapshot.Data.Tender(
                    id = id,
                    country = country,
                    title = title,
                    description = description,
                    auctionsStatus = auctionsStatus,
                    startDate = startDate,
                    endDate = endDate
                ),
                slots = slots,
                auctions = auctionsSnapshot
            )
        )
    }

    private fun scheduledAuctionsSnapshot(): List<TenderSnapshot.Data.Auction> {
        return scheduledAuctions.values.map { auction ->
            TenderSnapshot.Data.Auction(
                id = auction.id,
                lotId = auction.lotId,
                auctionPeriod = TenderSnapshot.Data.Auction.AuctionPeriod(
                    startDate = auction.startDate
                ),
                modalities = auction.modalities.map { modality ->
                    TenderSnapshot.Data.Auction.Modality(
                        url = modality.url,
                        eligibleMinimumDifference = modality.eligibleMinimumDifference.let { value ->
                            TenderSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                amount = value.amount,
                                currency = value.currency
                            )
                        }
                    )
                }
            )
        }
    }

    private fun startedAuctionsSnapshot(): List<TenderSnapshot.Data.Auction> {
        return startedAuctions.values.map { auction ->
            TenderSnapshot.Data.Auction(
                id = auction.id,
                lotId = auction.lotId,
                auctionPeriod = TenderSnapshot.Data.Auction.AuctionPeriod(
                    startDate = auction.startDate
                ),
                modalities = auction.modalities.map { modality ->
                    TenderSnapshot.Data.Auction.Modality(
                        url = modality.url,
                        eligibleMinimumDifference = modality.eligibleMinimumDifference.let { value ->
                            TenderSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                amount = value.amount,
                                currency = value.currency
                            )
                        }
                    )
                },
                title = auction.title,
                description = auction.description,
                value = auction.value.let { value ->
                    TenderSnapshot.Data.Auction.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                bids = auction.bids.map { bid ->
                    TenderSnapshot.Data.Auction.Bid(
                        id = bid.id,
                        owner = bid.owner,
                        relatedLot = bid.relatedLot,
                        value = bid.value.let { value ->
                            TenderSnapshot.Data.Auction.Bid.Value(
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
    }

    private fun endedAuctionsSnapshot(): List<TenderSnapshot.Data.Auction> {
        return endedAuctions.values.map { auction ->
            TenderSnapshot.Data.Auction(
                id = auction.id,
                lotId = auction.lotId,
                auctionPeriod = auction.period.let { period ->
                    TenderSnapshot.Data.Auction.AuctionPeriod(
                        startDate = period.startDate,
                        endDate = period.endDate
                    )
                },
                modalities = auction.modalities.map { modality ->
                    TenderSnapshot.Data.Auction.Modality(
                        url = modality.url,
                        eligibleMinimumDifference = modality.eligibleMinimumDifference.let { value ->
                            TenderSnapshot.Data.Auction.Modality.EligibleMinimumDifference(
                                amount = value.amount,
                                currency = value.currency
                            )
                        }
                    )
                },
                title = auction.title,
                description = auction.description,
                value = auction.value.let { value ->
                    TenderSnapshot.Data.Auction.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                bids = auction.bids.map { bid ->
                    TenderSnapshot.Data.Auction.Bid(
                        id = bid.id,
                        owner = bid.owner,
                        relatedLot = bid.relatedLot,
                        value = bid.value.let { value ->
                            TenderSnapshot.Data.Auction.Bid.Value(
                                amount = value.amount,
                                currency = value.currency
                            )
                        },
                        pendingDate = bid.pendingDate,
                        url = bid.url,
                        sign = bid.sign
                    )
                },
                progress = auction.progress.map { offer ->
                    TenderSnapshot.Data.Auction.Offer(
                        id = offer.id,
                        period = offer.period.let { period ->
                            TenderSnapshot.Data.Auction.Offer.Period(
                                startDate = period.startDate,
                                endDate = period.endDate
                            )
                        },
                        breakdowns = offer.breakdowns.map { breakdown ->
                            TenderSnapshot.Data.Auction.Offer.Breakdown(
                                relatedBid = breakdown.relatedBid,
                                status = breakdown.status,
                                dateMet = breakdown.dateMet,
                                value = breakdown.value.let { value ->
                                    TenderSnapshot.Data.Auction.Offer.Breakdown.Value(
                                        amount = value.amount,
                                        currency = value.currency
                                    )
                                }
                            )
                        }
                    )
                },
                results = auction.results.map { result ->
                    TenderSnapshot.Data.Auction.Result(
                        relatedBid = result.relatedBid,
                        value = result.value.let { value ->
                            TenderSnapshot.Data.Auction.Result.Value(
                                amount = value.amount,
                                currency = value.currency
                            )
                        }
                    )
                }
            )
        }
    }
}
