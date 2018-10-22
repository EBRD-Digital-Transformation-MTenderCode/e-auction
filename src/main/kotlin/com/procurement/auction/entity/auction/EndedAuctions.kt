package com.procurement.auction.entity.auction

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.AuctionId
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Currency
import com.procurement.auction.domain.ProgressId
import com.procurement.auction.domain.RelatedBid
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import java.time.LocalDateTime


data class EndedAuctions(
    val tender: Tender,
    val auctions: Auctions
) {
    @JsonPropertyOrder("cpid", "auctionPeriod")
    data class Tender(
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: CPID,
        @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod
    ) {
        @JsonPropertyOrder("startDate", "endDate")
        data class AuctionPeriod(
            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,
            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )
    }

    @JsonPropertyOrder("details")
    data class Auctions(
        @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
    ) {
        @JsonPropertyOrder("id",
            "relatedLot",
            "auctionPeriod",
            "electronicAuctionProgress",
            "electronicAuctionResult")
        data class Detail(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: RelatedLot,
            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("electronicAuctionProgress") @param:JsonProperty("electronicAuctionProgress") val electronicAuctionProgress: List<ElectronicAuctionProgress>,
            @field:JsonProperty("electronicAuctionResult") @param:JsonProperty("electronicAuctionResult") val electronicAuctionResult: List<ElectronicAuctionResult>
        ) {
            @JsonPropertyOrder("startDate", "endDate")
            data class AuctionPeriod(
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
            )

            @JsonPropertyOrder("id", "period", "breakdown")
            data class ElectronicAuctionProgress(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: ProgressId,
                @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,
                @field:JsonProperty("breakdown") @param:JsonProperty("breakdown") val breakdowns: List<Breakdown>
            ) {
                @JsonPropertyOrder("startDate", "endDate")
                data class Period(
                    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                    @JsonSerialize(using = JsonDateTimeSerializer::class)
                    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,
                    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                    @JsonSerialize(using = JsonDateTimeSerializer::class)
                    @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
                )

                @JsonPropertyOrder("relatedBid", "status", "dateMet", "value")
                data class Breakdown(
                    @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: RelatedBid,
                    @field:JsonProperty("status") @param:JsonProperty("status") val status: String = "met",
                    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                    @JsonSerialize(using = JsonDateTimeSerializer::class)
                    @field:JsonProperty("dateMet") @param:JsonProperty("dateMet") val dateMet: LocalDateTime,
                    @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                ) {
                    @JsonPropertyOrder("amount", "currency")
                    data class Value(
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                        @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                    )
                }
            }

            @JsonPropertyOrder("relatedBid", "value")
            data class ElectronicAuctionResult(
                @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: RelatedBid,
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
            ) {
                @JsonPropertyOrder("amount", "currency")
                data class Value(
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }
    }
}
