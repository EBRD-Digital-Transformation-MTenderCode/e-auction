package com.procurement.auction.domain.repository

import com.procurement.auction.domain.model.country.Country

interface CalendarRepository {
    fun loadWorkDays(country: Country, year: Int, month: Int): Set<Int>
}