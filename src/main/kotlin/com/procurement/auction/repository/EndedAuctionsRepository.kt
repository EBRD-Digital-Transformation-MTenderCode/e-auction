package com.procurement.auction.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.OperationId
import com.procurement.auction.entity.auction.EndedAuctions
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.repository.RepositoryProperties.KEY_SPACE
import com.procurement.auction.repository.RepositoryProperties.Tables
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject

interface EndedAuctionsRepository {
    fun load(cpid: CPID): EndedAuctions?
    fun save(cpid: CPID, operationId: OperationId, endedAuctions: EndedAuctions): EndedAuctions
}

class EndedAuctionsRepositoryImpl(
    private val objectMapper: ObjectMapper,
    private val session: Session
) : EndedAuctionsRepository {
    companion object {
        private const val loadEndedAuctionCQL =
            """SELECT
                ${Tables.EndedAuctions.columnTender},
                ${Tables.EndedAuctions.columnAuctions}
                 FROM $KEY_SPACE.${Tables.EndedAuctions.tableName}
                WHERE ${Tables.EndedAuctions.columnCpid}=?
            """

        private const val insertEndedAuctionCQL =
            """INSERT INTO $KEY_SPACE.${Tables.EndedAuctions.tableName}
               (
                ${Tables.EndedAuctions.columnCpid},
                ${Tables.EndedAuctions.columnOperationId},
                ${Tables.EndedAuctions.columnOperationDate},
                ${Tables.EndedAuctions.columnTender},
                ${Tables.EndedAuctions.columnAuctions}
               )
               VALUES (?,?,?,?,?) IF NOT EXISTS
            """
    }

    private val preparedLoadEndedAuctionCQL = session.prepare(loadEndedAuctionCQL)
    private val preparedInsertEndedAuctionCQL = session.prepare(insertEndedAuctionCQL)

    override fun load(cpid: CPID): EndedAuctions? {
        val query = preparedLoadEndedAuctionCQL.bind().also {
            it.setString(RepositoryProperties.Tables.EndedAuctions.columnCpid, cpid)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when loading a record of the ended auction with the cpid: '$cpid'  in the database.")
        }

        return resultSet.one()?.let { row -> rowToEndedAuctions(row) }
    }

    override fun save(cpid: CPID, operationId: OperationId, endedAuctions: EndedAuctions): EndedAuctions {
        val tender = objectMapper.toJson(endedAuctions.tender)
        val auctions = objectMapper.toJson(endedAuctions.auctions)

        val query = preparedInsertEndedAuctionCQL.bind().also {
            it.setString(Tables.EndedAuctions.columnCpid, cpid)
            it.setUUID(Tables.EndedAuctions.columnOperationId, operationId)
            it.setString(Tables.EndedAuctions.columnTender, tender)
            it.setString(Tables.EndedAuctions.columnAuctions, auctions)
        }

        val resultSet = save(query)
        return if (resultSet.wasApplied())
            endedAuctions
        else
            rowToEndedAuctions(resultSet.one())
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

    private fun rowToTender(row: Row): EndedAuctions.Tender =
        objectMapper.toObject(row.getString(RepositoryProperties.Tables.StartedAuctions.columnTender))

    private fun rowToAuctions(row: Row): EndedAuctions.Auctions =
        objectMapper.toObject(row.getString(RepositoryProperties.Tables.StartedAuctions.columnAuctions))

    private fun rowToEndedAuctions(row: Row): EndedAuctions =
        EndedAuctions(tender = rowToTender(row), auctions = rowToAuctions(row))
}