package com.procurement.auction.infrastructure.dispatcher

import com.procurement.auction.configuration.properties.GlobalProperties
import org.springframework.http.HttpStatus

interface CodeError {
    val httpStatus: HttpStatus
    val code: String
}

enum class CodesOfErrors(final override val httpStatus: HttpStatus, group: String, id: String) : CodeError {
    BAD_PAYLOAD(                        httpStatus = HttpStatus.BAD_REQUEST,            group = "01", id = "01"),
    BAD_PAYLOAD_COMMAND(                httpStatus = HttpStatus.BAD_REQUEST,            group = "01", id = "02"),

    //Calendar
    NO_DATA_IN_CALENDAR(                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,  group = "02", id = "01"),

    //Execute command
    COMMAND_CANNOT_BE_EXECUTED(         httpStatus = HttpStatus.BAD_REQUEST,            group = "03", id = "01"),

    //Schedule
    OUT_OF_LOTS(                        httpStatus = HttpStatus.BAD_REQUEST,            group = "04", id = "01"),
    INCORRECT_NUMBER_MODALITIES(        httpStatus = HttpStatus.BAD_REQUEST,            group = "04", id = "02"),

    //Tender
    TENDER_NOT_FOUND(                   httpStatus = HttpStatus.NOT_FOUND,              group = "05", id = "01"),
    TENDER_IS_ALREADY(                  httpStatus = HttpStatus.BAD_REQUEST,            group = "05", id = "02"),

    //Validation Amount
    VALIDATION_ATTRIBUTE(               httpStatus = HttpStatus.BAD_REQUEST,            group = "06", id = "01"),
    VALIDATION_AMOUNT_VALUE(            httpStatus = HttpStatus.BAD_REQUEST,            group = "06", id = "02"),

    //Validation lots
    NO_LOTS(                            httpStatus = HttpStatus.BAD_REQUEST,            group = "07", id = "01"),
    DUPLICATE_LOT(                      httpStatus = HttpStatus.BAD_REQUEST,            group = "07", id = "02"),
    UNKNOWN_LOT(                        httpStatus = HttpStatus.BAD_REQUEST,            group = "07", id = "03"),

    //Validation bids
    NO_BIDS(                            httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "01"),
    DUPLICATE_BID(                      httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "02"),
    UNKNOWN_BID(                        httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "03"),
    BID_INCORRECT_NUMBER_LOTS(          httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "04"),
    BID_ON_UNKNOWN_LOT(                 httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "05"),
    UNKNOWN_BID_IN_BREAKDOWN(           httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "06"),
    UNKNOWN_BID_IN_RESULT(              httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "07"),
    DUPLICATE_BID_IN_BREAKDOWN(         httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "08"),
    DUPLICATE_BID_IN_RESULT(            httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "09"),
    INCORRECT_NUMBER_BIDS_IN_BREAKDOWN( httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "10"),
    INCORRECT_NUMBER_BIDS_IN_RESULT(    httpStatus = HttpStatus.BAD_REQUEST,            group = "08", id = "11"),

    //Common
    SERVER_ERROR(                       httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,  group = "00", id = "00");

    override val code: String = "${this.httpStatus.value()}.${GlobalProperties.serviceId}.$group.$id"

    override fun toString(): String = code
}
