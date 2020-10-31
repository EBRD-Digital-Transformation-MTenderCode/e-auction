package com.procurement.submission.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.service.Logger
import com.procurement.auction.application.service.Transform
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.infrastructure.service.command.type.Action
import com.procurement.auction.infrastructure.service.handler.Handler
import com.procurement.auction.infrastructure.web.request.tryGetId
import com.procurement.auction.infrastructure.web.request.tryGetVersion
import com.procurement.auction.infrastructure.web.response.ApiResponse2
import com.procurement.auction.infrastructure.web.response.ApiResponse2Generator.generateResponseOnFailure
import com.procurement.auction.infrastructure.web.response.ApiSuccessResponse2

abstract class AbstractHandler2<ACTION : Action, R>(
    val transform: Transform,
    private val logger: Logger
) : Handler<ACTION, ApiResponse2> {

    override fun handle(node: JsonNode): ApiResponse2 {
        val id = node.tryGetId().get
        val version = node.tryGetVersion().get

        return when (val result = execute(node)) {
            is Result.Success -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed. Result: '${transform.trySerialization(result.get)}'")
                return ApiSuccessResponse2(
                    version = version,
                    id = id,
                    result = result.get
                )
            }
            is Result.Failure -> generateResponseOnFailure(
                fail = result.error,
                version = version,
                id = id,
                logger = logger
            )
        }
    }

    abstract fun execute(node: JsonNode): Result<R, Fail>
}