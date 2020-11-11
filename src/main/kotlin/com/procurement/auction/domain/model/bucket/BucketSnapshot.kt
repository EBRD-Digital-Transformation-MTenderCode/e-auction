package com.procurement.auction.domain.model.bucket

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.date.JsonTimeDeserializer
import com.procurement.auction.domain.model.date.JsonTimeSerializer
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.domain.model.slots.id.SlotIdDeserializer
import com.procurement.auction.domain.model.slots.id.SlotIdSerializer
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion
import java.time.LocalTime

class BucketSnapshot(
    val id: BucketId,
    val apiVersion: ApiVersion,
    val rowVersion: RowVersion,
    val slots: SlotsSnapshot,
    val occupancy: OccupancySnapshot
) {
    @JsonPropertyOrder("slots")
    data class SlotsSnapshot(
        @field:JsonProperty("slots") @param:JsonProperty("slots") val slots: List<Slot>
    ) {
        @JsonPropertyOrder("slotId", "startTime", "endTime", "maxLines")
        data class Slot(
            @JsonDeserialize(using = SlotIdDeserializer::class)
            @JsonSerialize(using = SlotIdSerializer::class)
            @field:JsonProperty("slotId") @param:JsonProperty("slotId") val slotId: SlotId,

            @JsonDeserialize(using = JsonTimeDeserializer::class)
            @JsonSerialize(using = JsonTimeSerializer::class)
            @field:JsonProperty("startTime") @param:JsonProperty("startTime") val startTime: LocalTime,

            @JsonDeserialize(using = JsonTimeDeserializer::class)
            @JsonSerialize(using = JsonTimeSerializer::class)
            @field:JsonProperty("endTime") @param:JsonProperty("endTime") val endTime: LocalTime,

            @field:JsonProperty("maxLines") @param:JsonProperty("maxLines") val maxLines: Int
        )
    }

    @JsonPropertyOrder("occupancy")
    data class OccupancySnapshot(
        @field:JsonProperty("occupancy") @param:JsonProperty("occupancy") val occupancy: List<Detail>
    ) {
        @JsonPropertyOrder("slotId", "cpids")
        data class Detail(
            @JsonDeserialize(using = SlotIdDeserializer::class)
            @JsonSerialize(using = SlotIdSerializer::class)
            @field:JsonProperty("slotId") @param:JsonProperty("slotId") val slotId: SlotId,

            @field:JsonProperty("cpids") @param:JsonProperty("cpids") val cpids: List<Cpid>
        )
    }
}