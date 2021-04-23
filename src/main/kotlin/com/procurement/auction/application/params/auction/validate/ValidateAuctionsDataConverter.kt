package com.procurement.auction.application.params.auction.validate

import com.procurement.auction.domain.extension.mapResult
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.infrastructure.service.handler.auction.validate.ValidateAuctionsDataRequest

fun ValidateAuctionsDataRequest.convert(): Result<ValidateAuctionsDataParams, DataErrors> {
    val convertedTender = tender.convert().orForwardFail { return it }
    return ValidateAuctionsDataParams.tryCreate(convertedTender, operationType)
}

fun ValidateAuctionsDataRequest.Tender.convert(): Result<ValidateAuctionsDataParams.Tender, DataErrors> {
    val convertedLots = lots.map { it.convertLot() }
    val convertedValue = value?.let { ValidateAuctionsDataParams.Tender.Value(it.currency) }

    return ValidateAuctionsDataParams.Tender.tryCreate(
        electronicAuctions = electronicAuctions.convert().orForwardFail { fail -> return fail },
        lots = convertedLots,
        value = convertedValue
    )
}

private fun ValidateAuctionsDataRequest.Tender.Lot.convertLot() =
    ValidateAuctionsDataParams.Tender.Lot(
        id = id,
        value = value?.let { ValidateAuctionsDataParams.Tender.Value(it.currency) }
    )

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
                ValidateAuctionsDataParams.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference.tryCreate(
                    it.eligibleMinimumDifference.currency, it.eligibleMinimumDifference.amount
                ).orForwardFail { return it }
            )
        }
    )



