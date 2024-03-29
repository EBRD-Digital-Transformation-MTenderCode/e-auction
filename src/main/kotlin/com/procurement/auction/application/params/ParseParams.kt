package com.procurement.auction.application.params

import com.procurement.auction.domain.extension.tryParseLocalDateTime
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.fail.error.DataTimeError
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asFailure
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.model.Owner
import com.procurement.auction.domain.model.amount.Amount
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.enums.EnumElementProvider
import com.procurement.auction.domain.model.enums.EnumElementProvider.Companion.keysAsStrings
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.tryOwner
import java.math.BigDecimal
import java.time.LocalDateTime

fun parseCpid(value: String): Result<Cpid, DataErrors.Validation.DataMismatchToPattern> =
    Cpid.tryCreateOrNull(value = value)
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.DataMismatchToPattern(
                name = "cpid",
                pattern = Cpid.pattern,
                actualValue = value
            )
        )

fun parseOcid(value: String): Result<Ocid, DataErrors.Validation.DataMismatchToPattern> =
    Ocid.tryCreateOrNull(value = value)
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.DataMismatchToPattern(
                name = "ocid",
                pattern = Ocid.pattern,
                actualValue = value
            )
        )

fun <T> parseEnum(
    value: String, allowedEnums: Set<T>, attributeName: String, target: EnumElementProvider<T>
): Result<T, DataErrors.Validation.UnknownValue> where T : Enum<T>,
                                                       T : EnumElementProvider.Key =

    target.orNull(value)
        ?.takeIf { it in allowedEnums }
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.UnknownValue(
                name = attributeName,
                expectedValues = allowedEnums.keysAsStrings(),
                actualValue = value
            )
        )

fun parseDate(value: String, attributeName: String): Result<LocalDateTime, DataErrors.Validation> =
    value.tryParseLocalDateTime()
        .mapError { fail ->
            when (fail) {
                is DataTimeError.InvalidFormat -> DataErrors.Validation.DataFormatMismatch(
                    name = attributeName,
                    actualValue = value,
                    expectedFormat = fail.pattern
                )

                is DataTimeError.InvalidDateTime ->
                    DataErrors.Validation.InvalidDateTime(name = attributeName, actualValue = value)
            }
        }

fun parseOwner(value: String): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    value.tryOwner()
        .doReturn {
            return Result.failure(
                DataErrors.Validation.DataFormatMismatch(
                    name = "owner",
                    actualValue = value,
                    expectedFormat = "uuid"
                )
            )
        }
        .asSuccess()

fun parseAmount(
    value: BigDecimal, attributeName: String
): Result<Amount, DataErrors.Validation.DataMismatchToPattern> {
    return try {
        Amount(value).asSuccess()
    } catch (ex: IllegalArgumentException) {
        DataErrors.Validation.DataMismatchToPattern(
            name = attributeName,
            pattern = ex.message!!,
            actualValue = value.toString()
        ).asFailure()
    }
}