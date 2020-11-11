package com.procurement.auction.domain.model.tender.snapshot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.Ocid
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.amount.AmountDeserializer
import com.procurement.auction.domain.model.amount.AmountSerializer
import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.id.AuctionIdDeserializer
import com.procurement.auction.domain.model.auction.id.AuctionIdSerializer
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.auction.status.AuctionsStatusDeserializer
import com.procurement.auction.domain.model.auction.status.AuctionsStatusSerializer
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountryDeserializer
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.domain.model.slots.id.SlotsIdsDeserializer
import com.procurement.auction.domain.model.slots.id.SlotsIdsSerializer
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.ApiVersionDeserializer
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import com.procurement.auction.domain.model.version.RowVersion
import java.time.LocalDateTime

class ScheduledAuctionsSnapshot(
    val rowVersion: RowVersion,
    val operationId: OperationId,
    val ocid: Ocid,
    val data: Data
) {
    companion object {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    @JsonPropertyOrder("version", "tender", "slots", "auctions")
    class Data(
        @JsonDeserialize(using = ApiVersionDeserializer::class)
        @JsonSerialize(using = ApiVersionSerializer::class)
        @field:JsonProperty("version") @param:JsonProperty("version") val apiVersion: ApiVersion,

        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,

        @JsonDeserialize(using = SlotsIdsDeserializer::class)
        @JsonSerialize(using = SlotsIdsSerializer::class)
        @field:JsonProperty("slots") @param:JsonProperty("slots") val slots: Set<SlotId>,

        @field:JsonProperty("auctions") @param:JsonProperty("auctions") val auctions: List<Auction>
    ) {

        @JsonPropertyOrder("id", "country", "status", "startDate")
        class Tender(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: Cpid,

            @JsonDeserialize(using = CountryDeserializer::class)
            @JsonSerialize(using = CountrySerializer::class)
            @field:JsonProperty("country") @param:JsonProperty("country") val country: Country,

            @JsonDeserialize(using = AuctionsStatusDeserializer::class)
            @JsonSerialize(using = AuctionsStatusSerializer::class)
            @field:JsonProperty("status") @param:JsonProperty("status") val status: AuctionsStatus,

            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
        )

        class Auction(
            @JsonDeserialize(using = AuctionIdDeserializer::class)
            @JsonSerialize(using = AuctionIdSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,

            @JsonDeserialize(using = LotIdDeserializer::class)
            @JsonSerialize(using = LotIdSerializer::class)
            @field:JsonProperty("lotId") @param:JsonProperty("lotId") val lotId: LotId,

            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("modalities") @param:JsonProperty("modalities") val modalities: List<Modality>
        ) {
            class AuctionPeriod(
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
            )

            class Modality(
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String,
                @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
            ) {
                class EligibleMinimumDifference(
                    @JsonDeserialize(using = AmountDeserializer::class)
                    @JsonSerialize(using = AmountSerializer::class)
                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount?,

                    @JsonDeserialize(using = CurrencyDeserializer::class)
                    @JsonSerialize(using = CurrencySerializer::class)
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }
    }
}