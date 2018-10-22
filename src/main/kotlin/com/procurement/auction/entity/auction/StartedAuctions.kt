package com.procurement.auction.entity.auction

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.AuctionId
import com.procurement.auction.domain.BidId
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.PlatformId
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.Sign
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import java.time.LocalDateTime

class StartedAuctions(
    val tender: Tender,
    val auctions: Auctions,
    val bidders: Bidders
) {
    @JsonPropertyOrder("cpid", "title", "description")
    data class Tender(
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: CPID,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String
    )

    @JsonPropertyOrder("details")
    data class Auctions(
        @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Auction>
    ) {
        @JsonPropertyOrder("id",
            "title",
            "description",
            "relatedLot",
            "eligibleMinimumDifference",
            "value",
            "auctionPeriod",
            "electronicAuctionModalities")
        data class Auction(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: RelatedLot,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>

        ) {
            @JsonPropertyOrder("amount", "currency")
            data class Value(
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Country
            )

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
                data class EligibleMinimumDifference(@field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                                                     @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Country
                )
            }
        }
    }

    @JsonPropertyOrder("details")
    data class Bidders(
        @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Bidder>
    ) {
        @JsonPropertyOrder("platformId", "bids")
        data class Bidder(
            @field:JsonProperty("platformId") @param:JsonProperty("platformId") val platformId: PlatformId,
            @field:JsonProperty("bids") @param:JsonProperty("bids") val bids: List<Bid>) {

            @JsonPropertyOrder("id", "relatedLot", "pendingDate", "value", "url", "sign")
            data class Bid(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: BidId,
                @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: RelatedLot,
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("pendingDate") @param:JsonProperty("pendingDate") val pendingDate: LocalDateTime,
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String,
                @field:JsonProperty("sign") @param:JsonProperty("sign") val sign: Sign
            ) {
                @JsonPropertyOrder("amount", "currency")
                data class Value(
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Country
                )
            }
        }
    }
}