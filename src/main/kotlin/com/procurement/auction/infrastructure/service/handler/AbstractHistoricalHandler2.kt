package com.procurement.auction.infrastructure.service.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.service.Logger
import com.procurement.auction.application.service.Transform
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.repository.HistoryRepository
import com.procurement.auction.infrastructure.repository.HistoryRepositoryCassandra
import com.procurement.auction.infrastructure.service.command.type.Action
import com.procurement.auction.infrastructure.web.request.tryGetId
import com.procurement.auction.infrastructure.web.request.tryGetVersion
import com.procurement.auction.infrastructure.web.response.ApiResponseV2
import com.procurement.auction.infrastructure.web.response.ApiResponse2Generator.generateResponseOnFailure
import com.procurement.auction.infrastructure.web.response.ApiSuccessResponse2

abstract class AbstractHistoricalHandler2<ACTION : Action, R>(
    private val target: Class<R>,
    private val historyRepository: HistoryRepository,
    val transform: Transform,
    private val logger: Logger
) : Handler<ACTION, ApiResponseV2> {

    override fun handle(node: JsonNode): ApiResponseV2 {
        val id = node.tryGetId().get
        val version = node.tryGetVersion().get

        val history = historyRepository.getHistory(id, action)
            .doOnError { error ->
                return generateResponseOnFailure(
                    fail = error, version = version, id = id, logger = logger
                )
            }
            .get
        if (history != null) {
            val result = transform.tryDeserialization(value = history, target = target)
                .doReturn { incident ->
                    return generateResponseOnFailure(
                        fail = Fail.Incident.Database.Parsing(
                            column = HistoryRepositoryCassandra.JSON_DATA,
                            value = history,
                            exception = incident.exception
                        ),
                        id = id,
                        version = version,
                        logger = logger
                    )
                }
            return ApiSuccessResponse2(version = version, id = id, result = result)
        }

        return when (val result = execute(node)) {
            is Result.Success -> {
                val resultData = result.get
                if (resultData != null)
                    historyRepository.saveHistory(id, action, resultData)
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed. Result: '${transform.trySerialization(result.get)}'")

                ApiSuccessResponse2(version = version, id = id, result = resultData)
            }
            is Result.Failure -> generateResponseOnFailure(
                fail = result.error, version = version, id = id, logger = logger
            )
        }
    }

    abstract fun execute(node: JsonNode): Result<R?, Fail>
}

