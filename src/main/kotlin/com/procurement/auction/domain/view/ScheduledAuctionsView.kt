package com.procurement.auction.domain.view

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.amount.AmountDeserializer
import com.procurement.auction.domain.model.amount.AmountSerializer
import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.id.AuctionIdDeserializer
import com.procurement.auction.domain.model.auction.id.AuctionIdSerializer
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import java.time.LocalDateTime

@JsonPropertyOrder("auctionPeriod", "electronicAuctions")
data class ScheduledAuctionsView(
    @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
    @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions
) : View {

    @JsonPropertyOrder("startDate")
    data class AuctionPeriod(
        @JsonSerialize(using = JsonDateTimeSerializer::class)
        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
    )

    data class ElectronicAuctions(
        @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
    ) {
        @JsonPropertyOrder("id", "relatedLot", "auctionPeriod", "electronicAuctionModalities")
        data class Detail(
            @JsonSerialize(using = AuctionIdSerializer::class)
            @JsonDeserialize(using = AuctionIdDeserializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,

            @JsonSerialize(using = LotIdSerializer::class)
            @JsonDeserialize(using = LotIdDeserializer::class)
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
        ) {
            @JsonPropertyOrder("startDate")
            data class AuctionPeriod(
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
            )

            @JsonPropertyOrder("url", "eligibleMinimumDifference")
            data class ElectronicAuctionModality(
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String,
                @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
            ) {
                @JsonPropertyOrder("amount", "currency")
                data class EligibleMinimumDifference(
                    @JsonSerialize(using = AmountSerializer::class)
                    @JsonDeserialize(using = AmountDeserializer::class)
                    @JsonFormat(shape = JsonFormat.Shape.STRING)
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                    @JsonSerialize(using = CurrencySerializer::class)
                    @JsonDeserialize(using = CurrencyDeserializer::class)
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }
    }
}
