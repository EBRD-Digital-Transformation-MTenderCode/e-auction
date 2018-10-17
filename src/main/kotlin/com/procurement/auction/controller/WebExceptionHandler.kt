package com.procurement.auction.controller

import com.procurement.auction.configuration.properties.GlobalProperties
import com.procurement.auction.domain.response.CodesOfErrors
import com.procurement.auction.domain.response.ErrorRS
import com.procurement.auction.exception.CalendarNoDataException
import com.procurement.auction.exception.JsonParseToObjectException
import com.procurement.auction.exception.NoLotsForAuctionPlanningException
import com.procurement.auction.exception.OutOfAuctionException
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
        val log: Logger = LoggerFactory.getLogger(ScheduleController::class.java)
    }

    @ExceptionHandler(value = [JsonParseToObjectException::class])
    fun jsonParseToObjectException(exception: JsonParseToObjectException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
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
    fun outOfAuctionException(exception: OutOfAuctionException): ResponseEntity<ErrorRS> {
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

    @ExceptionHandler(value = [NoLotsForAuctionPlanningException::class])
    fun noLotsForBookingAuctionException(exception: NoLotsForAuctionPlanningException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.OK.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.NO_LOT_FOR_AUCTIONS_PLANNING.code,
                            description = exception.message!!
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [CalendarNoDataException::class])
    fun calendarNoDataException(exception: CalendarNoDataException): ResponseEntity<ErrorRS> {
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
