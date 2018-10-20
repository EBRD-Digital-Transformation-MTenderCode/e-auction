package com.procurement.auction.domain.schedule

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.LotId
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.binding.ApiVersionDeserializer
import com.procurement.auction.domain.binding.ApiVersionSerializer
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import java.time.LocalDateTime
import java.util.*

@JsonPropertyOrder("version", "startDate", "usedSlots", "lots")
data class PlannedAuction(
    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,
    @JsonSerialize(using = JsonDateTimeSerializer::class)
    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDateTime: LocalDateTime,
    @field:JsonProperty("usedSlots") @param:JsonProperty("usedSlots") val usedSlots: Set<Int>,
    @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: LinkedHashMap<RelatedLot, Lot>) {

    @JsonPropertyOrder("id", "startDate", "electronicAuctionModalities")
    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
        @JsonSerialize(using = JsonDateTimeSerializer::class)
        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDateTime: LocalDateTime,
        @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
    ) {
        @JsonPropertyOrder("url", "eligibleMinimumDifference")
        data class ElectronicAuctionModality(
            @field:JsonProperty("url") @param:JsonProperty("url") val url: String,
            @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
        ) {
            @JsonPropertyOrder("amount", "currency")
            data class EligibleMinimumDifference(
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Double,
                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
            )
        }
    }
}
