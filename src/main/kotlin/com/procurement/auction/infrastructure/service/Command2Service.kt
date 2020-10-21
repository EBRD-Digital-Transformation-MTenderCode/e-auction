package com.procurement.auction.infrastructure.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.service.Logger
import com.procurement.auction.infrastructure.service.command.type.Command2Type
import com.procurement.auction.infrastructure.web.request.tryGetAction
import com.procurement.auction.infrastructure.web.request.tryGetId
import com.procurement.auction.infrastructure.web.request.tryGetVersion
import com.procurement.auction.infrastructure.web.response.ApiResponse2
import com.procurement.auction.infrastructure.web.response.ApiResponse2Generator.generateResponseOnFailure
import org.springframework.stereotype.Service

@Service
class Command2Service(
    private val logger: Logger
) {

    fun execute(node: JsonNode): ApiResponse2 {

        val version = node.tryGetVersion()
            .doReturn { versionFail ->
                val id = node.tryGetId()
                    .doReturn { idFail -> return generateResponseOnFailure(fail = idFail, logger = logger) }
                return generateResponseOnFailure(fail = versionFail, logger = logger, id = id)
            }

        val id = node.tryGetId()
            .doReturn { fail ->
                return generateResponseOnFailure(fail = fail, version = version, logger = logger)
            }

        val action = node.tryGetAction()
            .doReturn { error ->
                return generateResponseOnFailure(fail = error, id = id, version = version, logger = logger)
            }

        return when (action) {
            Command2Type.TODO -> TODO()
            else -> TODO("delete else case")
        }
    }
}