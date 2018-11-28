package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.repository.CalendarRepository
import com.procurement.auction.exception.database.ReadOperationException
import org.springframework.stereotype.Repository

@Repository
class CalendarRepositoryImpl(session: Session) : AbstractRepository(session), CalendarRepository {
    companion object {
        private const val tableName = "calendar"
        private const val columnCountry = "country"
        private const val columnYear = "year"
        private const val columnMonth = "month"
        private const val columnWorkDays = "work_days"

        private const val loadCalendarCQL = """
                SELECT $columnWorkDays
                  FROM $KEY_SPACE.$tableName
                 WHERE $columnCountry=?
                   AND $columnYear=?
                   AND $columnMonth=?;"""
    }

    private val preparedLoadCalendarCQL = session.prepare(loadCalendarCQL)

    override fun loadWorkDays(country: Country, year: Int, month: Int): Set<Int> {
        val query = preparedLoadCalendarCQL.bind().also {
            it.setString(columnCountry, CountrySerializer.serialize(country))
            it.setInt(columnYear, year)
            it.setInt(columnMonth, month)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when reading a record for country: '$country', year: $year, month: $month in the database.")
        }

        return resultSet.one()?.getSet(0, Int::class.javaObjectType) ?: emptySet()
    }
}