package com.procurement.auction.application.params.auction.validate

import com.procurement.auction.domain.extension.mapResult
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.infrastructure.service.handler.auction.validate.ValidateAuctionsDataRequest

fun ValidateAuctionsDataRequest.convert(): Result<ValidateAuctionsDataParams, DataErrors> =
    ValidateAuctionsDataParams(
        ValidateAuctionsDataParams.Tender.tryCreate(
            electronicAuctions = tender.electronicAuctions.convert().orForwardFail { fail -> return fail },
            lots = convertLots(),
            value = ValidateAuctionsDataParams.Tender.Value(tender.value.currency)
        ).orForwardFail { fail -> return fail }
    ).asSuccess()

private fun ValidateAuctionsDataRequest.convertLots() =
    tender.lots.map { ValidateAuctionsDataParams.Tender.Lot(it.id) }

private fun ValidateAuctionsDataRequest.Tender.ElectronicAuctions.convert() =
    ValidateAuctionsDataParams.Tender.ElectronicAuctions.tryCreate(
        details.mapResult { it.convert() }.orForwardFail { fail -> return fail }
    )

private fun ValidateAuctionsDataRequest.Tender.ElectronicAuctions.Detail.convert() =
    ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.tryCreate(
        id = id,
        relatedLot = relatedLot,
        electronicAuctionModalities = electronicAuctionModalities.map {
            ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                    it.eligibleMinimumDifference.currency
                )
            )
        }
    )



