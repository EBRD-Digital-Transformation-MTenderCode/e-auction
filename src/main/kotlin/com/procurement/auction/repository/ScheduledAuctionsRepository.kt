package com.procurement.auction.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.OperationId
import com.procurement.auction.entity.schedule.ScheduledAuctions
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.repository.RepositoryProperties.KEY_SPACE
import com.procurement.auction.repository.RepositoryProperties.Tables
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

data class ScheduledAuctionKey(val cpid: CPID,
                               val operationId: OperationId,
                               val operationDate: LocalDateTime)

interface ScheduledAuctionsRepository {
    fun load(cpid: CPID, operationId: OperationId): ScheduledAuctions?
    fun loadLast(cpid: CPID): ScheduledAuctions?
    fun insert(key: ScheduledAuctionKey, scheduledAuctions: ScheduledAuctions): ScheduledAuctions
}

class ScheduledAuctionsRepositoryImpl(private val objectMapper: ObjectMapper,
                                      private val session: Session) : ScheduledAuctionsRepository {
    companion object {
        private const val loadJsonOperationHistoryCQL =
            """SELECT ${Tables.ScheduledAuctions.columnOperationDate},${Tables.ScheduledAuctions.columnAuctions}
                 FROM $KEY_SPACE.${Tables.ScheduledAuctions.tableName}
                WHERE ${Tables.ScheduledAuctions.columnCpid}=?
                  AND ${Tables.ScheduledAuctions.columnOperationId}=?"""

        private const val loadLastJsonOperationHistoryCQL =
            """SELECT ${Tables.ScheduledAuctions.columnOperationDate},${Tables.ScheduledAuctions.columnAuctions}
                 FROM $KEY_SPACE.${Tables.ScheduledAuctions.tableName}
                WHERE ${Tables.ScheduledAuctions.columnCpid}=?"""

        private const val insertOperationHistoryCQL =
            """INSERT INTO $KEY_SPACE.${Tables.ScheduledAuctions.tableName} (
                ${Tables.ScheduledAuctions.columnCpid},
                ${Tables.ScheduledAuctions.columnOperationId},
                ${Tables.ScheduledAuctions.columnOperationDate},
                ${Tables.ScheduledAuctions.columnAuctions})
                VALUES (?,?,?,?) IF NOT EXISTS;"""
    }

    private val preparedLoadJsonOperationHistoryCQL = session.prepare(loadJsonOperationHistoryCQL)
    private val preparedLoadLastJsonOperationHistoryCQL = session.prepare(loadLastJsonOperationHistoryCQL)
    private val preparedInsertOperationHistoryCQL = session.prepare(insertOperationHistoryCQL)

    override fun load(cpid: CPID, operationId: OperationId): ScheduledAuctions? {
        val query = preparedLoadJsonOperationHistoryCQL.bind().also {
            it.setString(Tables.ScheduledAuctions.columnCpid, cpid)
            it.setUUID(Tables.ScheduledAuctions.columnOperationId, operationId)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when reading a record of scheduled auctions with the cpid: '$cpid' and operationId: '$operationId' in the database.")
        }

        return resultSet.all()
            .asSequence()
            .maxBy { it.getTimestamp(Tables.ScheduledAuctions.columnOperationDate) }
            ?.getString(Tables.ScheduledAuctions.columnAuctions)
            ?.let { json ->
                objectMapper.toObject<ScheduledAuctions>(json)
            }
    }

    override fun loadLast(cpid: CPID): ScheduledAuctions? {
        val query = preparedLoadLastJsonOperationHistoryCQL.bind().also {
            it.setString(Tables.ScheduledAuctions.columnCpid, cpid)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when reading a record of last scheduled auctions with the cpid: '$cpid' in the database.")
        }

        return resultSet.all()
            .asSequence()
            .maxBy { it.getTimestamp(Tables.ScheduledAuctions.columnOperationDate) }
            ?.getString(Tables.ScheduledAuctions.columnAuctions)
            ?.let { json ->
                objectMapper.toObject<ScheduledAuctions>(json)
            }
    }

    override fun insert(key: ScheduledAuctionKey, scheduledAuctions: ScheduledAuctions): ScheduledAuctions {
        val json = objectMapper.toJson(scheduledAuctions)
        val query = preparedInsertOperationHistoryCQL.bind().also {
            it.setString(Tables.ScheduledAuctions.columnCpid, key.cpid)
            it.setUUID(Tables.ScheduledAuctions.columnOperationId, key.operationId)
            val operationDate: Date = Date.from(key.operationDate.toInstant(ZoneOffset.UTC))
            it.setTimestamp(Tables.ScheduledAuctions.columnOperationDate, operationDate)
            it.setString(Tables.ScheduledAuctions.columnAuctions, json)
        }

        val resultSet = save(query)
        return if (resultSet.wasApplied()) {
            scheduledAuctions
        } else {
            val previousScheduledAuctions: String = resultSet.one().getString(Tables.ScheduledAuctions.columnAuctions)
            objectMapper.toObject(previousScheduledAuctions)
        }
    }

    private fun load(statement: BoundStatement) = try {
        session.execute(statement)
    } catch (ex: Exception) {
        throw ReadOperationException(message = "Error read from the database.", cause = ex)
    }

    private fun save(statement: BoundStatement) = try {
        session.execute(statement)
    } catch (ex: Exception) {
        throw SaveOperationException(message = "Error writing to the database.", cause = ex)
    }
}