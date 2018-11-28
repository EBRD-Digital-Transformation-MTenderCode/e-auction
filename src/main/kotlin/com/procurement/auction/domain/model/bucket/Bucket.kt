package com.procurement.auction.domain.model.bucket

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.warn
import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.slots.Slot
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.service.AllocationStrategy
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import java.time.LocalDateTime

data class AuctionsTimes(
    val startDateTime: LocalDateTime,
    val slotsIds: Set<SlotId>,
    val items: Map<LotId, LocalDateTime>
)

class Bucket(
    val id: BucketId,
    private var rowVersion: RowVersion,
    private val slots: Map<SlotId, Slot>,
    private val allocationStrategy: AllocationStrategy
) {
    companion object {
        private val log: Logger = Slf4jLogger()

        val apiVersion = ApiVersion(0, 0, 1)
    }

    val isNew: Boolean
        get() = rowVersion.isNew

    fun booking(cpid: CPID, estimates: List<EstimatedDurationAuction>): AuctionsTimes? {
        val auctionsTimes = allocationStrategy.allocation(id.date, estimates, slots.values)
        if (auctionsTimes != null) {
            for (slotId in auctionsTimes.slotsIds) {
                if (slots[slotId]!!.booking(cpid))
                    log.warn { "Bucket with id: '$id' is already cpid: '$cpid' in slot: '$slotId'." }
            }

            rowVersion = rowVersion.next()
        }

        return auctionsTimes
    }

    fun release(cpid: CPID, slotsIds: Set<SlotId>): Boolean {
        var hasChanged = false
        slotsIds.forEach { slotId ->
            if (!slots[slotId]!!.release(cpid))
                log.warn { "Bucket with id: '$id' no contains cpid: '$cpid' in slot: '$slotId'." }
            else
                hasChanged = true
        }

        if (hasChanged) rowVersion = rowVersion.next()
        return hasChanged
    }

    fun toSnapshot(): BucketSnapshot {
        val slotSnapshotData = mutableListOf<BucketSnapshot.SlotsSnapshot.Slot>()
        val occupancySnapshotData = mutableListOf<BucketSnapshot.OccupancySnapshot.Detail>()
        for (slot in slots.values) {
            slotSnapshotData.add(
                BucketSnapshot.SlotsSnapshot.Slot(
                    slotId = slot.id,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    maxLines = slot.maxLines
                )
            )
            occupancySnapshotData.add(
                BucketSnapshot.OccupancySnapshot.Detail(
                    slotId = slot.id,
                    cpids = slot.cpids.toSet()
                )
            )
        }

        return BucketSnapshot(
            id = id,
            rowVersion = rowVersion,
            apiVersion = apiVersion,
            slots = BucketSnapshot.SlotsSnapshot(slots = slotSnapshotData),
            occupancy = BucketSnapshot.OccupancySnapshot(occupancy = occupancySnapshotData)
        )
    }

    override fun toString(): String = "Bucket ($id, $rowVersion, slots: '$slots')"
}