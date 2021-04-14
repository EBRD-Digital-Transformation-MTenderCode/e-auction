package com.procurement.auction.application.params.auction.validate

import com.procurement.auction.application.params.parseAmount
import com.procurement.auction.application.params.parseEnum
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.functional.validate
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.enums.OperationType
import com.procurement.submission.application.params.rules.notEmptyRule
import java.math.BigDecimal

class ValidateAuctionsDataParams private constructor(
    val tender: Tender,
    val operationType: OperationType
) {
    companion object {
        private val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.CREATE_PCR,
                    OperationType.CREATE_RFQ -> true
                }
            }
            .toSet()

        fun tryCreate(tender: Tender, operationType: String): Result<ValidateAuctionsDataParams, DataErrors.Validation> {
            val parsedOperationType = parseEnum(
                value = operationType, allowedEnums = allowedOperationType,
                attributeName = "operationType",
                target = OperationType
            ).orForwardFail { return  it }

            return ValidateAuctionsDataParams(tender = tender, operationType = parsedOperationType).asSuccess()
        }
    }

    class Tender private constructor(
        val electronicAuctions: ElectronicAuctions,
        val lots: List<Lot>,
        val value: Value?
    ) {
        companion object {
            private const val LOTS_ATTRIBUTE_NAME = "tender.lots"

            fun tryCreate(
                electronicAuctions: ElectronicAuctions,
                lots: List<Lot>,
                value: Value?
            ): Result<Tender, DataErrors> {
                lots.validate(notEmptyRule(LOTS_ATTRIBUTE_NAME))
                    .orForwardFail { fail -> return fail }
                return Tender(
                    electronicAuctions,
                    lots,
                    value
                ).asSuccess()
            }
        }

        class ElectronicAuctions private constructor(
            val details: List<Detail>
        ) {
            companion object {
                private const val ELECTRONIC_AUCTIONS_ATTRIBUTE_NAME = "tender.electronicAuctions.details"

                fun tryCreate(
                    details: List<Detail>
                ): Result<ElectronicAuctions, DataErrors> {
                    details.validate(notEmptyRule(ELECTRONIC_AUCTIONS_ATTRIBUTE_NAME))
                        .orForwardFail { fail -> return fail }
                    return ElectronicAuctions(
                        details
                    ).asSuccess()
                }
            }

            class Detail private constructor(
                val id: String,
                val relatedLot: String,
                val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) {
                companion object {
                    private const val ELECTRONIC_AUCTIONS_MODALITIES_ATTRIBUTE_NAME = "tender.electronicAuctions.details.electronicAuctionModalities"

                    fun tryCreate(
                        id: String,
                        relatedLot: String,
                        electronicAuctionModalities: List<ElectronicAuctionModality>
                    ): Result<Detail, DataErrors> {
                        electronicAuctionModalities.validate(notEmptyRule(ELECTRONIC_AUCTIONS_MODALITIES_ATTRIBUTE_NAME))
                            .orForwardFail { fail -> return fail }
                        return Detail(id, relatedLot, electronicAuctionModalities
                        ).asSuccess()
                    }
                }

                data class ElectronicAuctionModality(
                    val eligibleMinimumDifference: EligibleMinimumDifference
                ) {
                    class EligibleMinimumDifference private constructor(
                        val currency: String,
                        val amount: Amount?
                    ) {
                        companion object {
                            fun tryCreate(
                                currency: String,
                                amount: BigDecimal?
                            ): Result<EligibleMinimumDifference, DataErrors> {
                                val amount = amount?.let {
                                    parseAmount(
                                        value = amount,
                                        attributeName = "tender.electronicAuctions.details.electronicAuctionModalities.eligibleMinimumDifference.amount"
                                    ).orForwardFail { return it }

                                }
                                return EligibleMinimumDifference(currency, amount).asSuccess()
                            }
                        }
                    }
                }
            }
        }

        data class Lot(
            val id: String,
            val value: Value?
        )

        data class Value(
            val currency: String
        )
    }
}