package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class CalendarNoDataException(country: Country, year: Int, month: Int) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.NO_DATA_IN_CALENDAR,
    message = "Calendar no contains data for country: ${country.value}, year: $year, month: $month"
)