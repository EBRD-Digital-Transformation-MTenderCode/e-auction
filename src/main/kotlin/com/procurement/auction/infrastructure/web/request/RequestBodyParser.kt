package com.procurement.auction.infrastructure.web.request

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.service.Transform
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.fail.error.BadRequest
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asFailure
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.functional.bind
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.extension.tryGetAttribute
import com.procurement.auction.infrastructure.extension.tryGetAttributeAsEnum
import com.procurement.auction.infrastructure.extension.tryGetTextAttribute
import com.procurement.auction.infrastructure.service.command.type.Command2Type
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2

fun JsonNode.tryGetVersion(): Result<ApiVersion2, DataErrors> {
    val name = "version"
    return tryGetTextAttribute(name).bind {
        when (val result = ApiVersion2.tryValueOf(it)) {
            is Result.Success -> result
            is Result.Failure -> Result.failure(
                DataErrors.Validation.DataFormatMismatch(
                    name = name,
                    expectedFormat = "00.00.00",
                    actualValue = it
                )
            )
        }
    }
}

fun JsonNode.tryGetAction(): Result<Command2Type, DataErrors> =
    tryGetAttributeAsEnum("action", Command2Type)

fun <T : Any> JsonNode.tryGetParams(target: Class<T>, transform: Transform): Result<T, Fail.Error> {
    val name = "params"
    return tryGetAttribute(name).bind {
        when (val result = transform.tryMapping(it, target)) {
            is Result.Success -> result
            is Result.Failure -> Result.failure(
                BadRequest("Error parsing '$name'")
            )
        }
    }
}

fun <T : Any> JsonNode.tryGetData(target: Class<T>, transform: Transform): Result<T, Fail.Error> =
    when (val result = transform.tryMapping(this, target)) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(
            BadRequest("Error parsing 'data'")
        )
    }

fun JsonNode.tryGetId(): Result<CommandId, DataErrors> {
    val name = "id"
    return tryGetTextAttribute(name)
        .bind {
            CommandId.tryCreateOrNull(it)
                ?.asSuccess<CommandId, DataErrors>()
                ?: DataErrors.Validation.DataFormatMismatch(
                    name = name,
                    actualValue = it,
                    expectedFormat = CommandId.pattern
                ).asFailure()
        }
}

fun String.tryGetNode(transform: Transform): Result<JsonNode, BadRequest> =
    when (val result = transform.tryParse(this)) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(BadRequest())
    }
