package com.procurement.auction.domain.response.schedule

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.binding.ApiVersionDeserializer
import com.procurement.auction.domain.binding.ApiVersionSerializer
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import java.time.LocalDateTime

@JsonPropertyOrder("id", "data", "version")
data class ScheduleRS(@field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                      @field:JsonProperty("data") @param:JsonProperty("data") val data: Data,
                      @JsonDeserialize(using = ApiVersionDeserializer::class)
                      @JsonSerialize(using = ApiVersionSerializer::class)
                      @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion) {

    @JsonPropertyOrder("auctionPeriod", "electronicAuctions")
    data class Data(@field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
                    @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions)

    @JsonPropertyOrder("startDate")
    data class AuctionPeriod(@JsonSerialize(using = JsonDateTimeSerializer::class)
                             @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                             @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
    )

    data class ElectronicAuctions(@field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
    )

    @JsonPropertyOrder("id", "relatedLot", "auctionPeriod", "electronicAuctionModalities")
    data class Detail(@field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                      @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String,
                      @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriodLot,
                      @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
    )

    @JsonPropertyOrder("startDate")
    data class AuctionPeriodLot(@JsonSerialize(using = JsonDateTimeSerializer::class)
                                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
    )

    @JsonPropertyOrder("url", "eligibleMinimumDifference")
    data class ElectronicAuctionModality(@field:JsonProperty("url") @param:JsonProperty("url") val url: String,
                                         @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference)

    @JsonPropertyOrder("amount", "currency")
    data class EligibleMinimumDifference(@field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Double,
                                         @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
    )
}