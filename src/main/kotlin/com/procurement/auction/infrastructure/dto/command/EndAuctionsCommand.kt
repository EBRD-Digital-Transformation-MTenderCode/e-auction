package com.procurement.auction.infrastructure.dto.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.amount.AmountDeserializer
import com.procurement.auction.domain.model.amount.AmountSerializer
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.bid.id.BidIdDeserializer
import com.procurement.auction.domain.model.bid.id.BidIdSerializer
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.domain.model.command.name.CommandName
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountryDeserializer
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.operationId.OperationIdDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdSerializer
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.domain.model.progressId.ProgressIdDeserializer
import com.procurement.auction.domain.model.progressId.ProgressIdSerializer
import com.procurement.auction.domain.model.tender.TenderId
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class EndAuctionsCommand(
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,
    @field:JsonProperty("id") @param:JsonProperty("id") val id: CommandId,
    @field:JsonProperty("command") @param:JsonProperty("command") val name: CommandName,
    @field:JsonProperty("context") @param:JsonProperty("context") val context: Context,
    @field:JsonProperty("data") @param:JsonProperty("data") val data: Data
) {

    data class Context(
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: Cpid,
        @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: Ocid,

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

    data class Data(
        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
    ) {

        data class Tender(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: TenderId,

            @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
            @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions
        ) {

            data class AuctionPeriod(
                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
            )

            data class ElectronicAuctions(
                @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
            ) {

                data class Detail(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @JsonDeserialize(using = LotIdDeserializer::class)
                    @JsonSerialize(using = LotIdSerializer::class)
                    @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId,

                    @field:JsonProperty("auctionPeriod") @param:JsonProperty("auctionPeriod") val auctionPeriod: AuctionPeriod,
                    @field:JsonProperty("electronicAuctionProgress") @param:JsonProperty("electronicAuctionProgress") val electronicAuctionProgress: List<ElectronicAuctionProgress>,
                    @field:JsonProperty("electronicAuctionResult") @param:JsonProperty("electronicAuctionResult") val electronicAuctionResult: List<ElectronicAuctionResult>
                ) {

                    data class AuctionPeriod(
                        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                        @JsonSerialize(using = JsonDateTimeSerializer::class)
                        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                        @JsonSerialize(using = JsonDateTimeSerializer::class)
                        @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
                    )

                    data class ElectronicAuctionProgress(
                        @JsonDeserialize(using = ProgressIdDeserializer::class)
                        @JsonSerialize(using = ProgressIdSerializer::class)
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: ProgressId,

                        @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,
                        @field:JsonProperty("breakdown") @param:JsonProperty("breakdown") val breakdowns: List<Breakdown>
                    ) {

                        data class Period(
                            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                            @JsonSerialize(using = JsonDateTimeSerializer::class)
                            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                            @JsonSerialize(using = JsonDateTimeSerializer::class)
                            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
                        )

                        data class Breakdown(
                            @JsonDeserialize(using = BidIdDeserializer::class)
                            @JsonSerialize(using = BidIdSerializer::class)
                            @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

                            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                            @JsonSerialize(using = JsonDateTimeSerializer::class)
                            @field:JsonProperty("dateMet") @param:JsonProperty("dateMet") val dateMet: LocalDateTime,

                            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                        ) {

                            data class Value(
                                @JsonDeserialize(using = AmountDeserializer::class)
                                @JsonSerialize(using = AmountSerializer::class)
                                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount
                            )
                        }
                    }

                    data class ElectronicAuctionResult(
                        @JsonDeserialize(using = BidIdDeserializer::class)
                        @JsonSerialize(using = BidIdSerializer::class)
                        @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: BidId,

                        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
                    ) {

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
