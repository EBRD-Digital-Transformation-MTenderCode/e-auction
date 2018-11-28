package com.procurement.auction.domain.model.period

import java.time.LocalDateTime

class Period(val startDate: LocalDateTime, val endDate: LocalDateTime) {

    init {
        if (endDate.isBefore(startDate))
            throw IllegalArgumentException("The end date before the start date.")
    }

    companion object {
        fun of(period: Period) = Period(period.startDate, period.endDate)
    }
}