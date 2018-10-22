package com.procurement.auction.domain.request.auction

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.ApiVersion
import com.procurement.auction.domain.BidId
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.CommandId
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.Currency
import com.procurement.auction.domain.LotId
import com.procurement.auction.domain.OperationId
import com.procurement.auction.domain.OwnerId
import com.procurement.auction.domain.RelatedLot
import com.procurement.auction.domain.TendererId
import com.procurement.auction.domain.binding.ApiVersionDeserializer
import com.procurement.auction.domain.binding.ApiVersionSerializer
import com.procurement.auction.domain.binding.JsonDateTimeDeserializer
import com.procurement.auction.domain.binding.JsonDateTimeSerializer
import com.procurement.auction.domain.request.Command
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("id", "command", "context", "data", "version")
data class StartRQ(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,
    @field:JsonProperty("command") @param:JsonProperty("command") val command: Command,
    @field:JsonProperty("context") @param:JsonProperty("context") val context: Context,
    @field:JsonProperty("data") @param:JsonProperty("data") val data: Data,
    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion
) {
    @JsonPropertyOrder("cpid", "operationId", "startDate", "pmd", "country")
    data class Context(
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: CPID,
        @field:JsonProperty("operationId") @param:JsonProperty("operationId") val operationId: OperationId,
        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
        @JsonSerialize(using = JsonDateTimeSerializer::class)
        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val operationDate: LocalDateTime,
        @field:JsonProperty("pmd") @param:JsonProperty("pmd") val pmd: String,
        @field:JsonProperty("country") @param:JsonProperty("country") val country: Country)

    @JsonPropertyOrder("tender", "bidsData")
    data class Data(
        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,
        @field:JsonProperty("bidsData") @param:JsonProperty("bidsData") val bidsData: List<BidData>
    ) {

        @JsonPropertyOrder("id", "title", "electronicAuctionModalities")
        data class Tender(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPID,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
        ) {
            @JsonPropertyOrder("id", "title", "description", "value")
            data class Lot(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
            ) {
                @JsonPropertyOrder("amount", "currency")
                data class Value(
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }

        @JsonPropertyOrder("owner", "bids")
        data class BidData(
            @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: OwnerId,
            @field:JsonProperty("bids") @param:JsonProperty("bids") val bids: List<Bid>
        ) {

            @JsonPropertyOrder("id", "relatedLots", "createdDate", "pendingDate", "value", "tenderers")
            data class Bid(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: BidId,
                @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<RelatedLot>,
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("createdDate") @param:JsonProperty("createdDate") val createdDate: LocalDateTime,
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("pendingDate") @param:JsonProperty("pendingDate") val pendingDate: LocalDateTime,
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
                @field:JsonProperty("tenderers") @param:JsonProperty("tenderers") val tenderers: List<Tenderer>
            ) {
                @JsonPropertyOrder("id", "name")
                data class Tenderer(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: TendererId,
                    @field:JsonProperty("name") @param:JsonProperty("name") val name: String
                )

                @JsonPropertyOrder("amount", "currency")
                data class Value(
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }
    }
}
