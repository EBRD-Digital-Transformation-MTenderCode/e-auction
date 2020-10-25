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
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.cpid.CPIDDeserializer
import com.procurement.auction.domain.model.cpid.CPIDSerializer
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.platformId.PlatformId
import com.procurement.auction.domain.model.platformId.PlatformIdDeserializer
import com.procurement.auction.domain.model.platformId.PlatformIdSerializer
import com.procurement.auction.domain.model.sign.Sign
import com.procurement.auction.domain.model.sign.SignDeserializer
import com.procurement.auction.domain.model.sign.SignSerializer
import java.time.LocalDateTime

@JsonPropertyOrder("isAuctionStarted", "auctionsLinks", "electronicAuctions", "auctionsData")
data class StartedAuctionsView(
    @JvmField @field:JsonProperty("isAuctionStarted") @param:JsonProperty("isAuctionStarted") val isAuctionStarted: Boolean,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("auctionsLinks") @param:JsonProperty("auctionsLinks") val auctionsLinks: List<AuctionsLink>? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions? = null,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("auctionsData") @param:JsonProperty("auctionsData") val auctionsData: AuctionsData? = null
) : View {
    @JsonPropertyOrder("owner", "links")
    data class AuctionsLink(
        @JsonDeserialize(using = PlatformIdDeserializer::class)
        @JsonSerialize(using = PlatformIdSerializer::class)
        @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: PlatformId,

        @field:JsonProperty("links") @param:JsonProperty("links") val links: List<Link>
    ) {
        @JsonPropertyOrder("relatedBid", "url")
        data class Link(
            @JsonDeserialize(using = BidIdDeserializer::class)
            @JsonSerialize(using = BidIdSerializer::class)
            @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

            @field:JsonProperty("url") @param:JsonProperty("url") val url: String
        )
    }

    @JsonPropertyOrder("details")
    data class ElectronicAuctions(
        @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
    ) {
        @JsonPropertyOrder("id", "relatedLot", "auctionPeriod", "electronicAuctionModalities")
        data class Detail(
            @JsonDeserialize(using = AuctionIdDeserializer::class)
            @JsonSerialize(using = AuctionIdSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,

            @JsonSerialize(using = LotIdSerializer::class)
            @JsonDeserialize(using = LotIdDeserializer::class)
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
        ) {
            @JsonPropertyOrder("startDate")
            data class AuctionPeriod(
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
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
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                    @JsonSerialize(using = CurrencySerializer::class)
                    @JsonDeserialize(using = CurrencyDeserializer::class)
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }
    }

    @JsonPropertyOrder("tender", "bids")
    data class AuctionsData(
        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,
        @field:JsonProperty("bids") @param:JsonProperty("bids") val bids: List<Bid>
    ) {
        @JsonPropertyOrder("id", "title", "description", "tender")
        data class Tender(
            @JsonSerialize(using = CPIDSerializer::class)
            @JsonDeserialize(using = CPIDDeserializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPID,

            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
        ) {
            @JsonPropertyOrder("id", "title", "description", "eligibleMinimumDifference", "value", "auctionPeriod")
            data class Lot(
                @JsonSerialize(using = LotIdSerializer::class)
                @JsonDeserialize(using = LotIdDeserializer::class)
                @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,

                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                @JsonSerialize(using = AmountSerializer::class)
                @JsonDeserialize(using = AmountDeserializer::class)
                @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: Amount,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value?,

                @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod
            ) {
                @JsonPropertyOrder("amount", "currency")
                data class Value(
                    @JsonSerialize(using = AmountSerializer::class)
                    @JsonDeserialize(using = AmountDeserializer::class)
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                    @JsonSerialize(using = CurrencySerializer::class)
                    @JsonDeserialize(using = CurrencyDeserializer::class)
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )

                @JsonPropertyOrder("startDate")
                data class AuctionPeriod(
                    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                    @JsonSerialize(using = JsonDateTimeSerializer::class)
                    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime

                )
            }
        }

        @JsonPropertyOrder("id", "value", "relatedLot", "pendingDate", "sign")
        data class Bid(
            @JsonDeserialize(using = BidIdDeserializer::class)
            @JsonSerialize(using = BidIdSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: BidId,

            @JsonDeserialize(using = AmountDeserializer::class)
            @JsonSerialize(using = AmountSerializer::class)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Amount,

            @JsonSerialize(using = LotIdSerializer::class)
            @JsonDeserialize(using = LotIdDeserializer::class)
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("pendingDate") @param:JsonProperty("pendingDate") val pendingDate: LocalDateTime,

            @JsonDeserialize(using = SignDeserializer::class)
            @JsonSerialize(using = SignSerializer::class)
            @field:JsonProperty("sign") @param:JsonProperty("sign") val sign: Sign
        )
    }
}