package com.procurement.auction.domain.response.auction

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.AuctionId
import com.procurement.auction.domain.BidId
import com.procurement.auction.domain.CommandId
import com.procurement.auction.domain.Currency
import com.procurement.auction.domain.LotId
import com.procurement.auction.domain.OwnerId
import com.procurement.auction.domain.RelatedBid
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.Sign
import com.procurement.auction.domain.TendererId
import com.procurement.auction.domain.binding.ApiVersionDeserializer
import com.procurement.auction.domain.binding.ApiVersionSerializer
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import java.time.LocalDateTime

@JsonPropertyOrder("id", "command", "context", "data", "version")
data class StartRS(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,
    @field:JsonProperty("data") @param:JsonProperty("data") val data: Data,
    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion
) {
    @JsonPropertyOrder("isAuctionStarted", "auctionsLinks", "electronicAuctions", "auctionsData")
    data class Data(
        @JvmField @field:JsonProperty("isAuctionStarted") @param:JsonProperty("isAuctionStarted") val isAuctionStarted: Boolean,
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("auctionsLinks") @param:JsonProperty("auctionsLinks") val auctionsLinks: List<AuctionsLink>? = null,
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions? = null,
        @field:JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("auctionsData") @param:JsonProperty("auctionsData") val auctionsData: AuctionsData? = null
    ) {
        @JsonPropertyOrder("owner", "links")
        data class AuctionsLink(
            @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: OwnerId,
            @field:JsonProperty("links") @param:JsonProperty("links") val links: List<Link>
        ) {
            @JsonPropertyOrder("relatedBid", "url")
            data class Link(
                @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: RelatedBid,
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String
            )
        }

        @JsonPropertyOrder("details")
        data class ElectronicAuctions(
            @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
        ) {
            @JsonPropertyOrder("id", "relatedLot", "auctionPeriod", "electronicAuctionModalities")
            data class Detail(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,
                @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: RelatedLot,
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
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
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
            @JsonPropertyOrder("id", "title", "description", "auctions")
            data class Tender(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: TendererId,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
            ) {
                @JsonPropertyOrder("id", "title", "description", "eligibleMinimumDifference", "value", "auctionPeriod")
                data class Lot(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
                    @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                    @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                    @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: Double,
                    @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
                    @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod
                ) {
                    @JsonPropertyOrder("amount", "currency")
                    data class Value(
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
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
                @field:JsonProperty("id") @param:JsonProperty("id") val id: BidId,
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Amount,
                @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: RelatedLot,
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("pendingDate") @param:JsonProperty("pendingDate") val pendingDate: LocalDateTime,
                @field:JsonProperty("sign") @param:JsonProperty("sign") val sign: Sign
            )
        }
    }
}
