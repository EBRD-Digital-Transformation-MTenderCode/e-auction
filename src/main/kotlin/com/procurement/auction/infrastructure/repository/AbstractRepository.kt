package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException

abstract class AbstractRepository(private val session: Session) {
    companion object {
        const val KEY_SPACE = "auctions"
    }

    protected fun load(statement: BoundStatement): ResultSet = try {
        session.execute(statement)
    } catch (ex: Exception) {
        val message = if (ex.message != null)
            "Error read from the database. ${ex.message}"
        else
            "Error read from the database."
        throw ReadOperationException(message = message, cause = ex)
    }

    protected fun save(statement: BoundStatement): ResultSet = try {
        session.execute(statement)
    } catch (ex: Exception) {
        val message = if (ex.message != null)
            "Error writing to the database. ${ex.message}"
        else
            "Error writing to the database."
        throw SaveOperationException(message = message, cause = ex)
    }

    protected fun save(statement: BatchStatement): ResultSet = try {
        session.execute(statement)
    } catch (ex: Exception) {
        val message = if (ex.message != null)
            "Error writing to the database. ${ex.message}"
        else
            "Error writing to the database."
        throw SaveOperationException(message = message, cause = ex)
    }

    protected fun resultSetToString(resultSet: ResultSet?): String {
        if (resultSet == null) return "No result set."

        val sb = StringBuilder("ResultSet: ")
        resultSet.onEach { row ->
            for (definition in resultSet.columnDefinitions) {
                sb.appendln("name: '${definition.name}', value: '${row.getObject(definition.name).toString()}'")
            }
        }
        return sb.toString()
    }
}