package com.procurement.auction.domain.model.migration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.amount.AmountDeserializer
import com.procurement.auction.domain.model.amount.AmountSerializer
import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.id.AuctionIdDeserializer
import com.procurement.auction.domain.model.auction.id.AuctionIdSerializer
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.time.LocalDateTime

data class OldAuctions(
    val cpid: Cpid,
    val operationId: OperationId,
    val operationDate: LocalDateTime,
    val data: Data
) {

    data class Data(
        @field:JsonProperty("version") @param:JsonProperty("version") val apiVersion: ApiVersion,

        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
        @JsonSerialize(using = JsonDateTimeSerializer::class)
        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

        @JsonProperty("usedSlots") val usedSlots: List<Int>,
        @JsonProperty("lots") val lots: Map<String, Lot>
    ) {
        data class Lot(
            @JsonDeserialize(using = AuctionIdDeserializer::class)
            @JsonSerialize(using = AuctionIdSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: AuctionId,

            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @JsonProperty("url") val url: String,

            @JsonDeserialize(using = AmountDeserializer::class)
            @JsonSerialize(using = AmountSerializer::class)
            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

            @JsonDeserialize(using = CurrencyDeserializer::class)
            @JsonSerialize(using = CurrencySerializer::class)
            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
        )
    }
}