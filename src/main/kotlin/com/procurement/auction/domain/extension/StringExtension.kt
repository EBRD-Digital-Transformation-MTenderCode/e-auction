package com.procurement.auction.domain.extension

import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asFailure
import com.procurement.auction.domain.functional.asSuccess
import java.util.*

fun String.tryUUID(): Result<UUID, Fail.Incident.Transform.Parsing> =
    try {
        Result.success(UUID.fromString(this))
    } catch (ex: Exception) {
        Result.failure(
            Fail.Incident.Transform.Parsing(UUID::class.java.canonicalName, ex)
        )
    }

fun String.tryToLong(): Result<Long, Fail.Incident.Transform.Parsing> = try {
    this.toLong().asSuccess()
} catch (exception: NumberFormatException) {
    Fail.Incident.Transform.Parsing(className = Long::class.java.canonicalName, exception = exception).asFailure()
}

private val TRUE = Result.Success(true)
private val FALSE = Result.Success(false)
private val BOOLEAN_PARSE_FAIL = Result.failure(Fail.Incident.Transform.Parsing(className = Boolean::class.java.canonicalName))

fun String.tryToBoolean(): Result<Boolean, Fail.Incident.Transform.Parsing> =
    when {
        this.equals("true", ignoreCase = true) -> TRUE
        this.equals("false", ignoreCase = true) -> FALSE
        else -> BOOLEAN_PARSE_FAIL
    }