package com.procurement.submission.application.params.rules

import com.procurement.auction.domain.extension.getDuplicate
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.ValidationResult
import com.procurement.auction.domain.functional.ValidationRule

fun <T : Collection<Any>?> noDuplicatesRule(attributeName: String): ValidationRule<T, DataErrors.Validation> =
    ValidationRule { received: T ->
        val duplicate = received?.getDuplicate { it }
        if (duplicate != null)
            ValidationResult.error(
                DataErrors.Validation.UniquenessDataMismatch(
                    value = duplicate.toString(), name = attributeName
                )
            )
        else
            ValidationResult.ok()
    }

fun <T : Collection<Any>?> notEmptyRule(attributeName: String): ValidationRule<T, DataErrors.Validation> =
    ValidationRule { received: T ->
        if (received != null && received.isEmpty())
            ValidationResult.error(DataErrors.Validation.EmptyArray(attributeName))
        else
            ValidationResult.ok()
    }