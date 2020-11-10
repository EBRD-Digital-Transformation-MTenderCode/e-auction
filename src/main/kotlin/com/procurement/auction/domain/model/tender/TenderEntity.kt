package com.procurement.auction.domain.model.tender

import com.procurement.auction.domain.model.Ocid
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.tender.snapshot.CancelledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.EndedAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.deserialize

class TenderEntity(
    val rowVersion: RowVersion,
    val operationId: OperationId,
    val status: AuctionsStatus,
    val ocid: Ocid,
    private val data: String
) {
    fun toScheduledAuctionsSnapshot(deserializer: JsonDeserializeService): ScheduledAuctionsSnapshot {
        val data = deserializer.deserialize<ScheduledAuctionsSnapshot.Data>(data)
        return ScheduledAuctionsSnapshot(
            rowVersion = rowVersion,
            operationId = operationId,
            ocid = ocid,
            data = data
        )
    }

    fun toCancelledAuctionsSnapshot(deserializer: JsonDeserializeService): CancelledAuctionsSnapshot {
        val data = deserializer.deserialize<CancelledAuctionsSnapshot.Data>(data)
        return CancelledAuctionsSnapshot(
            rowVersion = rowVersion,
            operationId = operationId,
            ocid = ocid,
            data = data
        )
    }

    fun toStartedAuctionsSnapshot(deserializer: JsonDeserializeService): StartedAuctionsSnapshot {
        val data = deserializer.deserialize<StartedAuctionsSnapshot.Data>(data)
        return StartedAuctionsSnapshot(
            rowVersion = rowVersion,
            operationId = operationId,
            ocid = ocid,
            data = data
        )
    }

    fun toEndedAuctionsSnapshot(deserializer: JsonDeserializeService): EndedAuctionsSnapshot {
        val data = deserializer.deserialize<EndedAuctionsSnapshot.Data>(data)
        return EndedAuctionsSnapshot(
            rowVersion = rowVersion,
            operationId = operationId,
            ocid = ocid,
            data = data
        )
    }
}