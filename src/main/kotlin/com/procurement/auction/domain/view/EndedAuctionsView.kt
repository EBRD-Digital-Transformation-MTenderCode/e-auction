package com.procurement.auction.domain.view

import com.fasterxml.jackson.annotation.JsonInclude
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
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.bid.id.BidIdDeserializer
import com.procurement.auction.domain.model.bid.id.BidIdSerializer
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatus
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatusDeserializer
import com.procurement.auction.domain.model.breakdown.status.BreakdownStatusSerializer
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.domain.model.progressId.ProgressIdDeserializer
import com.procurement.auction.domain.model.progressId.ProgressIdSerializer
import java.time.LocalDateTime

@JsonPropertyOrder("tender")
data class EndedAuctionsView(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) : View {
    @JsonPropertyOrder("id", "auctionPeriod", "electronicAuctions")
    data class Tender(
        @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
        @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions
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

        @JsonPropertyOrder("details")
        data class ElectronicAuctions(
            @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
        ) {
            @JsonPropertyOrder("id",
                               "relatedLot",
                               "auctionPeriod",
                               "electronicAuctionModalities",
                               "electronicAuctionProgress",
                               "electronicAuctionResult")
            data class Detail(
                @JsonDeserialize(using = AuctionIdDeserializer::class)
                @JsonSerialize(using = AuctionIdSerializer::class)
                @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,

                @JsonSerialize(using = LotIdSerializer::class)
                @JsonDeserialize(using = LotIdDeserializer::class)
                @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

                @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
                @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>,
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

                @JsonPropertyOrder("url", "eligibleMinimumDifference")
                data class ElectronicAuctionModality(
                    @field:JsonProperty("url") @param:JsonProperty("url") val url: String,
                    @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
                ) {
                    @JsonPropertyOrder("amount", "currency")
                    data class EligibleMinimumDifference(
                        @JsonSerialize(using = AmountSerializer::class)
                        @JsonDeserialize(using = AmountDeserializer::class)
                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount?,

                        @JsonSerialize(using = CurrencySerializer::class)
                        @JsonDeserialize(using = CurrencyDeserializer::class)
                        @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                    )
                }

                @JsonPropertyOrder("id", "period", "breakdown")
                data class ElectronicAuctionProgress(
                    @JsonSerialize(using = ProgressIdSerializer::class)
                    @JsonDeserialize(using = ProgressIdDeserializer::class)
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
                        @JsonDeserialize(using = BidIdDeserializer::class)
                        @JsonSerialize(using = BidIdSerializer::class)
                        @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

                        @JsonDeserialize(using = BreakdownStatusDeserializer::class)
                        @JsonSerialize(using = BreakdownStatusSerializer::class)
                        @field:JsonProperty("status") @param:JsonProperty("status") val status: BreakdownStatus,

                        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                        @JsonSerialize(using = JsonDateTimeSerializer::class)
                        @field:JsonProperty("dateMet") @param:JsonProperty("dateMet") val dateMet: LocalDateTime,

                        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                    ) {
                        @JsonPropertyOrder("amount", "currency")
                        data class Value(
                            @JsonSerialize(using = AmountSerializer::class)
                            @JsonDeserialize(using = AmountDeserializer::class)
                            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                            @JsonSerialize(using = CurrencySerializer::class)
                            @JsonDeserialize(using = CurrencyDeserializer::class)
                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency?
                        )
                    }
                }

                @JsonPropertyOrder("relatedBid", "value")
                data class ElectronicAuctionResult(
                    @JsonDeserialize(using = BidIdDeserializer::class)
                    @JsonSerialize(using = BidIdSerializer::class)
                    @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

                    @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                ) {
                    @JsonPropertyOrder("amount", "currency")
                    data class Value(
                        @JsonSerialize(using = AmountSerializer::class)
                        @JsonDeserialize(using = AmountDeserializer::class)
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                        @JsonSerialize(using = CurrencySerializer::class)
                        @JsonDeserialize(using = CurrencyDeserializer::class)
                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency?
                    )
                }
            }
        }
    }
}
