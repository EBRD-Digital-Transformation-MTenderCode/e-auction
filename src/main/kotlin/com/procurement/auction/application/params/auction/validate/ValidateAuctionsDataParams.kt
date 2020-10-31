package com.procurement.auction.application.params.auction.validate

import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.functional.validate
import com.procurement.submission.application.params.rules.notEmptyRule

data class ValidateAuctionsDataParams(
    val tender: Tender
) {
    class Tender private constructor(
        val electronicAuctions: ElectronicAuctions,
        val lots: List<Lot>,
        val value: Value
    ) {
        companion object {
            private const val LOTS_ATTRIBUTE_NAME = "tender.lots"

            fun tryCreate(
                electronicAuctions: ElectronicAuctions,
                lots: List<Lot>,
                value: Value
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
                    data class EligibleMinimumDifference(
                        val currency: String
                    )
                }
            }
        }

        data class Lot(
            val id: String
        )

        data class Value(
            val currency: String
        )
    }
}