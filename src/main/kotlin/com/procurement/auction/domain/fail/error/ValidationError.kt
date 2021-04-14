package com.procurement.auction.domain.fail.error

import com.procurement.auction.domain.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR.COM-") {
    override val code: String = prefix + numberError

    class DuplicateElectronicAuctionIds(duplicateId: String) : ValidationError(
        numberError = "18.1.1",
        description = "Found duplicate electronicAuctions.details.id '$duplicateId'"
    )

    class LinkedLotNotFound(lotIds: Set<String>) : ValidationError(
        numberError = "18.1.2",
        description = "Missing lots: '${lotIds.joinToString()}'"
    )

    class LotMustBeLinkedToOneAuction(lotId: String): ValidationError(
        numberError = "18.1.3",
        description = "Lot '$lotId' must be linked to one and only one electronic auction"
    )

    class AuctionModalityMismatchWithTenderCurrency(auctionIds: List<String>, tenderCurrency: String): ValidationError(
        numberError = "18.1.4",
        description = "Electronic auctions '${auctionIds.joinToString()}' contain modality(s) with currency that does not match tender currency '$tenderCurrency'"
    )

    class AuctionModalityMismatchWithLotsCurrency(auctionIds: List<String>, lotsCurrencies: List<String>): ValidationError(
        numberError = "18.1.5",
        description = "Electronic auctions '${auctionIds.joinToString()}' contain modality(s) with currency that does not match lots currencies '$lotsCurrencies'"
    )

    class MissingTenderValue(): ValidationError(
        numberError = "18.1.6",
        description = "Missing tender's value in request."
    )

    class MissingLotValue(lotsId: Collection<String>): ValidationError(
        numberError = "18.1.7",
        description = "Missing value in lots $lotsId."
    )

    class MissingAmountValue(auctionId: String): ValidationError(
        numberError = "18.1.8",
        description = "Missing amount in auction '$auctionId'."
    )
}