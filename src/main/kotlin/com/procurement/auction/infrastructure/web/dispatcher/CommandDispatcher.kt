package com.procurement.auction.infrastructure.web.dispatcher

import com.procurement.auction.application.service.tender.TenderService
import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.logger.error
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.domain.model.command.name.CommandName.AUCTIONS_END
import com.procurement.auction.domain.model.command.name.CommandName.AUCTIONS_START
import com.procurement.auction.domain.model.command.name.CommandName.AUCTION_CANCEL
import com.procurement.auction.domain.model.command.name.CommandName.SCHEDULE
import com.procurement.auction.domain.model.command.name.CommandName.VALIDATE
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.deserialize
import com.procurement.auction.domain.view.CommandErrorView
import com.procurement.auction.domain.view.CommandSuccessView
import com.procurement.auction.domain.view.MessageErrorView
import com.procurement.auction.domain.view.View
import com.procurement.auction.exception.app.ApplicationException
import com.procurement.auction.exception.json.JsonParseToObjectException
import com.procurement.auction.infrastructure.dto.command.CancelAuctionsCommand
import com.procurement.auction.infrastructure.dto.command.Command
import com.procurement.auction.infrastructure.dto.command.EndAuctionsCommand
import com.procurement.auction.infrastructure.dto.command.ScheduleAuctionsCommand
import com.procurement.auction.infrastructure.dto.command.StartAuctionsCommand
import com.procurement.auction.infrastructure.dto.command.ValidateAuctionsCommand
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/command")
class CommandDispatcher(
    private val tenderService: TenderService,
    private val deserializer: JsonDeserializeService
) {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    @PostMapping()
    fun requestHandler(request: HttpServletRequest): ResponseEntity<*> {

        return try {
            val requestBody = request.reader.readText()
            val command: Command = deserializer.deserialize(requestBody)
            MDC.put("command-id", command.id.value.toString())
            MDC.put("command-name", command.name.code)
            MDC.put("command-context", command.context.toString())

            commandDispatcher(command, requestBody)
        } catch (exception: JsonParseToObjectException) {
            ResponseEntity.ok(
                MessageErrorView(
                    errors = listOf(
                        MessageErrorView.Error(
                            code = CodesOfErrors.BAD_PAYLOAD.code,
                            description = "The bad payload of request."
                        )
                    )
                )
            )
        } finally {
            MDC.remove("command-context")
            MDC.remove("command-name")
            MDC.remove("command-id")
        }
    }

    private fun commandDispatcher(command: Command, requestBody: String): ResponseEntity<*> {
        log.debug { "Retrieve command: $requestBody" }

        return when (command.name) {
            VALIDATE -> {
                commandProcessing(command) {
                    deserializer.deserialize<ValidateAuctionsCommand>(requestBody)
                        .let { tenderService.validateAuctions(it) }

                }

            }
            SCHEDULE -> {
                commandProcessing(command) {
                    deserializer.deserialize<ScheduleAuctionsCommand>(requestBody)
                        .let { tenderService.scheduleAuctions(it) }
                }
            }

            AUCTION_CANCEL -> {
                commandProcessing(command) {
                    deserializer.deserialize<CancelAuctionsCommand>(requestBody)
                        .let { tenderService.cancelAuctions(it) }
                }
            }

            AUCTIONS_START -> {
                commandProcessing(command) {
                    deserializer.deserialize<StartAuctionsCommand>(requestBody)
                        .let { tenderService.startAuctions(it) }
                }
            }

            AUCTIONS_END -> {
                commandProcessing(command) {
                    deserializer.deserialize<EndAuctionsCommand>(requestBody)
                        .let { tenderService.endAuctions(it) }
                }
            }
        }
    }

    private fun <T : View> commandProcessing(command: Command, block: () -> T): ResponseEntity<View> {
        try {
            val result = block()
            return ResponseEntity.ok(
                CommandSuccessView(
                    id = command.id,
                    version = GlobalProperties.App.apiVersion,
                    data = result
                )
            )
        } catch (exception: Throwable) {
            val errorBody = when (exception) {
                is JsonParseToObjectException -> {
                    log.error { exception.message!! }
                    errorView(
                        commandId = command.id,
                        codeError = CodesOfErrors.BAD_PAYLOAD_COMMAND,
                        description = "The bad payload of command."
                    )
                }

                is ApplicationException -> {
                    log.perform(level = exception.loglevel, exception = exception, message = exception.message!!)
                    errorView(
                        commandId = command.id,
                        codeError = exception.codeError,
                        description = exception.message!!
                    )
                }

                else -> {
                    log.error(exception) { exception.message!! }
                    errorView(
                        commandId = command.id,
                        codeError = CodesOfErrors.SERVER_ERROR,
                        description = if (log.isDebugEnabled)
                            exception.message ?: "Unknown server error."
                        else
                            "Unknown server error."
                    )
                }
            }

            return ResponseEntity.status(HttpStatus.OK.value()).body(errorBody)
        }
    }

    private fun errorView(commandId: CommandId, codeError: CodeError, description: String): View {
        return CommandErrorView(
            id = commandId,
            version = GlobalProperties.App.apiVersion,
            errors = listOf(
                CommandErrorView.Error(
                    code = codeError.code,
                    description = description
                )
            )
        )
    }
}