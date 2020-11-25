package com.procurement.auction.infrastructure.service.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.service.Logger
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.ValidationResult
import com.procurement.auction.infrastructure.service.command.type.Action
import com.procurement.auction.infrastructure.web.request.tryGetId
import com.procurement.auction.infrastructure.web.request.tryGetVersion
import com.procurement.auction.infrastructure.web.response.ApiResponseV2
import com.procurement.auction.infrastructure.web.response.ApiResponse2Generator.generateResponseOnFailure

abstract class AbstractValidationHandler2<ACTION : Action, E : Fail>(
    private val logger: Logger
) : Handler<ACTION, ApiResponseV2> {

    override fun handle(node: JsonNode): ApiResponseV2 {
        val id = node.tryGetId().get
        val version = node.tryGetVersion().get

        return when (val result = execute(node)) {
            is ValidationResult.Ok -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed.")
                ApiResponseV2.Success(version = version, id = id)
            }
            is ValidationResult.Fail -> generateResponseOnFailure(
                fail = result.error, version = version, id = id, logger = logger
            )
        }
    }

    abstract fun execute(node: JsonNode): ValidationResult<E>
}
