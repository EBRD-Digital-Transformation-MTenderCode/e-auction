package com.procurement.auction.infrastructure.service.handler.auction.validate

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class ValidateAuctionsDataRequest (
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("electronicAuctions") @field:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions,
        @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>,
        @param:JsonProperty("value") @field:JsonProperty("value") val value: Value
    ) {
        data class ElectronicAuctions(
            @param:JsonProperty("details") @field:JsonProperty("details") val details: List<Detail>
        ) {
            data class Detail(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: String,
                @param:JsonProperty("electronicAuctionModalities") @field:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) {
                data class ElectronicAuctionModality(
                    @param:JsonProperty("eligibleMinimumDifference") @field:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
                ) {
                    data class EligibleMinimumDifference(
                        @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
                    )
                }
            }
        }

        data class Lot(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String
        )

        data class Value(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
        )
    }
}