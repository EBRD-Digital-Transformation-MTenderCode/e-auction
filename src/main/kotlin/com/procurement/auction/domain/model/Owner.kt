package com.procurement.auction.domain.model

import com.procurement.auction.domain.extension.tryUUID
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import java.util.*

typealias Owner = UUID

fun String.tryOwner(): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    when (val result = this.tryUUID()) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "owner",
                actualValue = this,
                expectedFormat = "uuid"
            )
        )
    }