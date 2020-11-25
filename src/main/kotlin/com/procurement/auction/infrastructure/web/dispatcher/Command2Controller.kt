package com.procurement.auction.infrastructure.web.dispatcher

import com.procurement.auction.application.service.Logger
import com.procurement.auction.application.service.Transform
import com.procurement.auction.configuration.properties.GlobalProperties2
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.service.Command2Service
import com.procurement.auction.infrastructure.web.request.tryGetId
import com.procurement.auction.infrastructure.web.request.tryGetNode
import com.procurement.auction.infrastructure.web.request.tryGetVersion
import com.procurement.auction.infrastructure.web.response.ApiResponseV2
import com.procurement.auction.infrastructure.web.response.ApiResponse2Generator.generateResponseOnFailure
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/command2")
class Command2Controller(
    private val command2Service: Command2Service,
    private val transform: Transform,
    private val logger: Logger
) {

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ApiResponseV2> {
        if (logger.isDebugEnabled)
            logger.debug("RECEIVED COMMAND: '$requestBody'.")

        val node = requestBody.tryGetNode(transform)
            .doReturn { error -> return generateResponseEntityOnFailure(fail = error) }

        val version = when (val versionResult = node.tryGetVersion()) {
            is Result.Success -> versionResult.get
            is Result.Failure -> {
                return when (val idResult = node.tryGetId()) {
                    is Result.Success -> generateResponseEntityOnFailure(
                        fail = versionResult.error,
                        id = idResult.get
                    )
                    is Result.Failure -> generateResponseEntityOnFailure(fail = versionResult.error)
                }
            }
        }

        val id = node.tryGetId()
            .doReturn { error -> return generateResponseEntityOnFailure(fail = error, version = version) }

        val response =
            command2Service.execute(node)
                .also { response ->
                    if (logger.isDebugEnabled)
                        logger.debug("RESPONSE (id: '${id}'): '${transform.trySerialization(response)}'.")
                }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun generateResponseEntityOnFailure(
        fail: Fail, version: ApiVersion = GlobalProperties2.App.apiVersion, id: CommandId = CommandId.NaN
    ): ResponseEntity<ApiResponseV2> {
        val response = generateResponseOnFailure(
            fail = fail, id = id, version = version, logger = logger
        )
        return ResponseEntity(response, HttpStatus.OK)
    }
}