package com.procurement.auction.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.schedule.PlannedAuction
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.repository.RepositoryProperties.KEY_SPACE
import com.procurement.auction.repository.RepositoryProperties.Tables
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

data class PlannedAuctionKey(val cpid: String,
                             val operationId: UUID,
                             val operationDate: LocalDateTime)

interface PlannedAuctionRepository {
    fun load(cpid: String, operationId: UUID): PlannedAuction?
    fun insert(key: PlannedAuctionKey, plannedAuction: PlannedAuction): PlannedAuction
}

@Repository
class PlannedAuctionRepositoryImpl(private val objectMapper: ObjectMapper,
                                   private val session: Session) : PlannedAuctionRepository {
    companion object {
        private const val loadJsonOperationHistoryCQL =
            """SELECT ${Tables.AuctionPlanning.columnOperationDate},${Tables.AuctionPlanning.columnData}
                 FROM $KEY_SPACE.${Tables.AuctionPlanning.tableName}
                WHERE ${Tables.AuctionPlanning.columnCpid}=?
                  AND ${Tables.AuctionPlanning.columnOperationId}=?"""

        //toUnixTimestamp(now())
        private const val insertOperationHistoryCQL =
            """INSERT INTO $KEY_SPACE.${Tables.AuctionPlanning.tableName} (
                ${Tables.AuctionPlanning.columnCpid},
                ${Tables.AuctionPlanning.columnOperationId},
                ${Tables.AuctionPlanning.columnOperationDate},
                ${Tables.AuctionPlanning.columnData})
                VALUES (?,?,?,?) IF NOT EXISTS;"""
    }

    private val preparedLoadJsonOperationHistoryCQL = session.prepare(loadJsonOperationHistoryCQL)
    private val preparedInsertOperationHistoryCQL = session.prepare(insertOperationHistoryCQL)

    override fun load(cpid: String, operationId: UUID): PlannedAuction? {
        val query = preparedLoadJsonOperationHistoryCQL.bind().also {
            it.setString(Tables.AuctionPlanning.columnCpid, cpid)
            it.setUUID(Tables.AuctionPlanning.columnOperationId, operationId)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when reading a record with the cpid: '$cpid' in the database.")
        }

        return resultSet.all()
            .asSequence()
            .maxBy { it.getTimestamp(Tables.AuctionPlanning.columnOperationDate) }
            ?.getString(Tables.AuctionPlanning.columnData)
            ?.let { json ->
                objectMapper.toObject<PlannedAuction>(json)
            }
    }

    override fun insert(key: PlannedAuctionKey, plannedAuction: PlannedAuction): PlannedAuction {
        val json = objectMapper.toJson(plannedAuction)
        val query = preparedInsertOperationHistoryCQL.bind().also {
            it.setString(Tables.AuctionPlanning.columnCpid, key.cpid)
            it.setUUID(Tables.AuctionPlanning.columnOperationId, key.operationId)
            val operationDate: Date = Date.from(key.operationDate.toInstant(ZoneOffset.UTC))
            it.setTimestamp(Tables.AuctionPlanning.columnOperationDate, operationDate)
            it.setString(Tables.AuctionPlanning.columnData, json)
        }

        val resultSet = save(query)
        return if (resultSet.wasApplied()) {
            plannedAuction
        } else {
            val previousAuctionInfo = resultSet.one().getString(Tables.AuctionPlanning.columnData)
            objectMapper.toObject(previousAuctionInfo)
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