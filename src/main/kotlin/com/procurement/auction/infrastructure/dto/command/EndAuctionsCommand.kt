package com.procurement.auction.infrastructure.dto.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.amount.AmountDeserializer
import com.procurement.auction.domain.model.amount.AmountSerializer
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.bid.id.BidIdDeserializer
import com.procurement.auction.domain.model.bid.id.BidIdSerializer
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.domain.model.command.id.CommandIdDeserializer
import com.procurement.auction.domain.model.command.id.CommandIdSerializer
import com.procurement.auction.domain.model.command.name.CommandName
import com.procurement.auction.domain.model.command.name.CommandNameDeserializer
import com.procurement.auction.domain.model.command.name.CommandNameSerializer
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountryDeserializer
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.cpid.CPIDDeserializer
import com.procurement.auction.domain.model.cpid.CPIDSerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.operationId.OperationIdDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdSerializer
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.domain.model.progressId.ProgressIdDeserializer
import com.procurement.auction.domain.model.progressId.ProgressIdSerializer
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.ApiVersionDeserializer
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("version", "id", "command", "context", "data")
data class EndAuctionsCommand(
    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,

    @JsonDeserialize(using = CommandIdDeserializer::class)
    @JsonSerialize(using = CommandIdSerializer::class)
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,

    @JsonDeserialize(using = CommandNameDeserializer::class)
    @JsonSerialize(using = CommandNameSerializer::class)
    @field:JsonProperty("command") @param:JsonProperty("command") val name: CommandName,

    @field:JsonProperty("context") @param:JsonProperty("context") val context: Context,
    @field:JsonProperty("data") @param:JsonProperty("data") val data: Data
) {
    @JsonPropertyOrder("cpid", "operationId", "startDate", "pmd", "country")
    data class Context(
        @JsonDeserialize(using = CPIDDeserializer::class)
        @JsonSerialize(using = CPIDSerializer::class)
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: CPID,

        @JsonDeserialize(using = OperationIdDeserializer::class)
        @JsonSerialize(using = OperationIdSerializer::class)
        @field:JsonProperty("operationId") @param:JsonProperty("operationId") val operationId: OperationId,

        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
        @JsonSerialize(using = JsonDateTimeSerializer::class)
        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val operationDate: LocalDateTime,

        @field:JsonProperty("pmd") @param:JsonProperty("pmd") val pmd: String,

        @JsonDeserialize(using = CountryDeserializer::class)
        @JsonSerialize(using = CountrySerializer::class)
        @field:JsonProperty("country") @param:JsonProperty("country") val country: Country
    )

    @JsonPropertyOrder("tender")
    data class Data(
        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
    ) {
        @JsonPropertyOrder("id", "auctionPeriod", "electronicAuctions")
        data class Tender(
            @JsonDeserialize(using = CPIDDeserializer::class)
            @JsonSerialize(using = CPIDSerializer::class)
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPID,

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
                                   "electronicAuctionProgress",
                                   "electronicAuctionResult")
                data class Detail(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @JsonDeserialize(using = LotIdDeserializer::class)
                    @JsonSerialize(using = LotIdSerializer::class)
                    @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

                    @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
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

                    @JsonPropertyOrder("id", "period", "breakdown")
                    data class ElectronicAuctionProgress(
                        @JsonDeserialize(using = ProgressIdDeserializer::class)
                        @JsonSerialize(using = ProgressIdSerializer::class)
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

                        @JsonPropertyOrder("relatedBid", "dateMet", "value")
                        data class Breakdown(
                            @JsonDeserialize(using = BidIdDeserializer::class)
                            @JsonSerialize(using = BidIdSerializer::class)
                            @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

                            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                            @JsonSerialize(using = JsonDateTimeSerializer::class)
                            @field:JsonProperty("dateMet") @param:JsonProperty("dateMet") val dateMet: LocalDateTime,

                            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                        ) {
                            @JsonPropertyOrder("amount")
                            data class Value(
                                @JsonDeserialize(using = AmountDeserializer::class)
                                @JsonSerialize(using = AmountSerializer::class)
                                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount
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
                        @JsonPropertyOrder("amount")
                        data class Value(
                            @JsonDeserialize(using = AmountDeserializer::class)
                            @JsonSerialize(using = AmountSerializer::class)
                            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount
                        )
                    }
                }
            }
        }
    }
}