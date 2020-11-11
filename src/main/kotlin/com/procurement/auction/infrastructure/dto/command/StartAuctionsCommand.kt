package com.procurement.auction.infrastructure.dto.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.auction.domain.model.Ocid
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
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.currency.Currency
import com.procurement.auction.domain.model.currency.CurrencyDeserializer
import com.procurement.auction.domain.model.currency.CurrencySerializer
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.lotId.LotIdDeserializer
import com.procurement.auction.domain.model.lotId.LotIdSerializer
import com.procurement.auction.domain.model.lotId.LotsIdsDeserializer
import com.procurement.auction.domain.model.lotId.LotsIdsSerializer
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.operationId.OperationIdDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdSerializer
import com.procurement.auction.domain.model.platformId.PlatformId
import com.procurement.auction.domain.model.platformId.PlatformIdDeserializer
import com.procurement.auction.domain.model.platformId.PlatformIdSerializer
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.ApiVersionDeserializer
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class StartAuctionsCommand(
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
        @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,
        @field:JsonProperty("bidsData") @param:JsonProperty("bidsData") val bidsData: List<BidData>
    ) {

        data class Tender(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: Cpid,

            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value?
        ) {


            data class Value(
                @JsonDeserialize(using = AmountDeserializer::class)
                @JsonSerialize(using = AmountSerializer::class)
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                @JsonDeserialize(using = CurrencyDeserializer::class)
                @JsonSerialize(using = CurrencySerializer::class)
                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
            )

            data class Lot(
                @JsonDeserialize(using = LotIdDeserializer::class)
                @JsonSerialize(using = LotIdSerializer::class)
                @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,

                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value?
            ) {

                data class Value(
                    @JsonDeserialize(using = AmountDeserializer::class)
                    @JsonSerialize(using = AmountSerializer::class)
                    @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,

                    @JsonDeserialize(using = CurrencyDeserializer::class)
                    @JsonSerialize(using = CurrencySerializer::class)
                    @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: Currency
                )
            }
        }

        data class BidData(
            @JsonDeserialize(using = PlatformIdDeserializer::class)
            @JsonSerialize(using = PlatformIdSerializer::class)
            @field:JsonProperty("owner") @param:JsonProperty("owner") val owner: PlatformId,

            @field:JsonProperty("bids") @param:JsonProperty("bids") val bids: List<Bid>
        ) {

            data class Bid(
                @JsonDeserialize(using = BidIdDeserializer::class)
                @JsonSerialize(using = BidIdSerializer::class)
                @field:JsonProperty("id") @param:JsonProperty("id") val id: BidId,

                @JsonDeserialize(using = LotsIdsDeserializer::class)
                @JsonSerialize(using = LotsIdsSerializer::class)
                @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<LotId>,

                @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                @JsonSerialize(using = JsonDateTimeSerializer::class)
                @field:JsonProperty("pendingDate") @param:JsonProperty("pendingDate") val pendingDate: LocalDateTime,

                @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
            ) {

                data class Value(
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
