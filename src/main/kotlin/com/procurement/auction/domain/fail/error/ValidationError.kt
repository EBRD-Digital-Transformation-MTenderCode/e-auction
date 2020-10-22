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

    class AuctionModalityContainsInvalidCurrency(auctionIds: List<String>, tenderCurrency: String): ValidationError(
        numberError = "18.1.4",
        description = "Electronic auctions '${auctionIds.joinToString()}' contain modality(s) with currency that does not match tender currency '$tenderCurrency'"
    )
}