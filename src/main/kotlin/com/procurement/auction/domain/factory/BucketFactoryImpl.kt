package com.procurement.auction.domain.factory

import com.procurement.auction.domain.model.bucket.Bucket
import com.procurement.auction.domain.model.bucket.BucketSnapshot
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.slots.Slot
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.service.AllocationStrategy
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.deserialize
import org.springframework.stereotype.Service

@Service
class BucketFactoryImpl(
    private val deserializer: JsonDeserializeService,
    private val allocationStrategy: AllocationStrategy
) : BucketFactory {

    override fun create(id: BucketId,
                        rowVersion: RowVersion,
                        apiVersion: ApiVersion,
                        slots: String,
                        occupancy: String): Bucket {

        val bucketSnapshotSlots: BucketSnapshot.SlotsSnapshot = deserializer.deserialize(slots)
        val bucketSnapshotOccupancy: BucketSnapshot.OccupancySnapshot = deserializer.deserialize(occupancy)
        val cpids = bucketSnapshotOccupancy.occupancy.associateBy({ it.slotId }, { it.cpids })

        return Bucket(
            id = id,
            rowVersion = rowVersion,
            slots = bucketSnapshotSlots.slots.asSequence().map {
                Slot(
                    id = it.slotId,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    maxLines = it.maxLines,
                    cpids = cpids[it.slotId] ?: emptyList()
                )
            }.associateBy { it.id },
            allocationStrategy = allocationStrategy
        )
    }
}