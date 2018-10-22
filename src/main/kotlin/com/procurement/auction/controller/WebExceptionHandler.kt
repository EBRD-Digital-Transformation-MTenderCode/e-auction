package com.procurement.auction.controller

import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.domain.response.CodesOfErrors
import com.procurement.auction.domain.response.ErrorRS
import com.procurement.auction.exception.CalendarNoDataException
import com.procurement.auction.exception.JsonParseToObjectException
import com.procurement.auction.exception.LotInScheduledAuctionsNotFoundException
import com.procurement.auction.exception.LotInStartedAuctionNotFoundException
import com.procurement.auction.exception.NoLotsToScheduleAuctionsException
import com.procurement.auction.exception.OutOfAuctionException
import com.procurement.auction.exception.ScheduledAuctionsNotFoundException
import com.procurement.auction.exception.StartedAuctionNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class WebExceptionHandler : ResponseEntityExceptionHandler() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(WebExceptionHandler::class.java)
    }

    @ExceptionHandler(value = [JsonParseToObjectException::class])
    fun jsonParseToObject(exception: JsonParseToObjectException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.BAD_PAYLOAD.code,
                            description = "The bad payload of request."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [OutOfAuctionException::class])
    fun outOfAuction(exception: OutOfAuctionException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "400.${GlobalProperties.serviceId}.00",
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [NoLotsToScheduleAuctionsException::class])
    fun noLotsForBookingAuction(exception: NoLotsToScheduleAuctionsException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.NO_LOT_TO_SCHEDULE_AUCTIONS.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [CalendarNoDataException::class])
    fun calendarNoData(exception: CalendarNoDataException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.NO_DATA_IN_CALENDAR.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [ScheduledAuctionsNotFoundException::class])
    fun scheduledAuctionNotFound(exception: ScheduledAuctionsNotFoundException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.SCHEDULED_AUCTIONS_NOT_FOUND.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [LotInScheduledAuctionsNotFoundException::class])
    fun scheduledAuctionLotNotFound(exception: LotInScheduledAuctionsNotFoundException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.LOT_IN_SCHEDULED_AUCTIONS_NOT_FOUND.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [StartedAuctionNotFoundException::class])
    fun startedAuctionNotFound(exception: StartedAuctionNotFoundException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.STARTED_AUCTIONS_NOT_FOUND.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [LotInStartedAuctionNotFoundException::class])
    fun lotInStartedAuctionNotFound(exception: LotInStartedAuctionNotFoundException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.LOT_IN_STARTED_AUCTIONS_NOT_FOUND.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [Exception::class])
    fun otherExceptions(exception: Exception): ResponseEntity<ErrorRS> {
        log.error(exception.message, exception)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.SERVER_ERROR.code,
                            description = exception.message ?: "Unknown error."
                        )
                    )
                )
            )
    }
}
