package com.procurement.auction.domain.model.tender.snapshot

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.auction.status.AuctionsStatusDeserializer
import com.procurement.auction.domain.model.auction.status.AuctionsStatusSerializer
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountryDeserializer
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.cpid.CPIDDeserializer
import com.procurement.auction.domain.model.cpid.CPIDSerializer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.ApiVersionDeserializer
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import com.procurement.auction.domain.model.version.RowVersion

class CancelledAuctionsSnapshot(
    val rowVersion: RowVersion,
    val operationId: OperationId,
    val data: Data
) {
    companion object {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    @JsonPropertyOrder("version", "tender")
    class Data(
        @JsonDeserialize(using = ApiVersionDeserializer::class)
        @JsonSerialize(using = ApiVersionSerializer::class)
        @field:JsonProperty("version") @param:JsonProperty("version") val apiVersion: ApiVersion,

        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
    ) {

        @JsonPropertyOrder("id", "country", "status")
        class Tender(
            @JsonDeserialize(using = CPIDDeserializer::class)
            @JsonSerialize(using = CPIDSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPID,

            @JsonDeserialize(using = CountryDeserializer::class)
            @JsonSerialize(using = CountrySerializer::class)
            @field:JsonProperty("country") @param:JsonProperty("country") val country: Country,

            @JsonDeserialize(using = AuctionsStatusDeserializer::class)
            @JsonSerialize(using = AuctionsStatusSerializer::class)
            @field:JsonProperty("status") @param:JsonProperty("status") val status: AuctionsStatus
        )
    }
}