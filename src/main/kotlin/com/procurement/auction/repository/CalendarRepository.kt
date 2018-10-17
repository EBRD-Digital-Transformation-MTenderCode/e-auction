package com.procurement.auction.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.procurement.auction.exception.database.ReadOperationException
import org.springframework.stereotype.Repository

interface CalendarRepository {
    fun loadWorkDays(country: String, year: Int, month: Int): Set<Int>
}

@Repository
class CalendarRepositoryImpl(private val session: Session) : CalendarRepository {
    companion object {
        private const val loadCalendarCQL = """
                SELECT ${RepositoryProperties.Tables.Calendar.columnWorkDays}
                  FROM ${RepositoryProperties.KEY_SPACE}.${RepositoryProperties.Tables.Calendar.tableName}
                 WHERE ${RepositoryProperties.Tables.Calendar.columnCountry}=?
                   AND ${RepositoryProperties.Tables.Calendar.columnYear}=?
                   AND ${RepositoryProperties.Tables.Calendar.columnMonth}=?;"""
    }

    private val preparedLoadCalendarCQL = session.prepare(loadCalendarCQL)

    override fun loadWorkDays(country: String, year: Int, month: Int): Set<Int> {
        val query = preparedLoadCalendarCQL.bind().also {
            it.setString(RepositoryProperties.Tables.Calendar.columnCountry, country)
            it.setInt(RepositoryProperties.Tables.Calendar.columnYear, year)
            it.setInt(RepositoryProperties.Tables.Calendar.columnMonth, month)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when reading a record for country: '$country', year: $year, month: $month in the database.")
        }

        return resultSet.one()?.getSet(0, Int::class.javaObjectType) ?: emptySet()
    }

    private fun load(statement: BoundStatement) = try {
        session.execute(statement)
    } catch (ex: Exception) {
        throw ReadOperationException(message = "Error read from the database.", cause = ex)
    }
}