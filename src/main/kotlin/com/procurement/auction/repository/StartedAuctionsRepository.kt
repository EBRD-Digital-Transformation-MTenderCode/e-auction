package com.procurement.auction.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.OperationId
import com.procurement.auction.entity.auction.StartedAuctions
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.repository.RepositoryProperties.KEY_SPACE
import com.procurement.auction.repository.RepositoryProperties.Tables
import com.procurement.auction.service.toJson
import com.procurement.auction.service.toObject

interface StartedAuctionsRepository {
    fun load(cpid: CPID): StartedAuctions?
    fun loadAuctions(cpid: CPID): StartedAuctions.Auctions?
    fun save(cpid: CPID, operationId: OperationId, startedAuctions: StartedAuctions): StartedAuctions
}

class StartedAuctionsRepositoryImpl(
    private val objectMapper: ObjectMapper,
    private val session: Session
) : StartedAuctionsRepository {
    companion object {
        private const val loadStartedAuctionCQL =
            """SELECT
                ${Tables.StartedAuctions.columnTender},
                ${Tables.StartedAuctions.columnAuctions},
                ${Tables.StartedAuctions.columnBidders}
                 FROM $KEY_SPACE.${Tables.StartedAuctions.tableName}
                WHERE ${Tables.StartedAuctions.columnCpid}=?
            """

        private const val loadAuctionsCQL =
            """SELECT
                ${Tables.StartedAuctions.columnAuctions}
                 FROM $KEY_SPACE.${Tables.StartedAuctions.tableName}
                WHERE ${Tables.StartedAuctions.columnCpid}=?
            """

        private const val insertStartedAuctionCQL =
            """INSERT INTO $KEY_SPACE.${Tables.StartedAuctions.tableName}
               (
                ${Tables.StartedAuctions.columnCpid},
                ${Tables.StartedAuctions.columnOperationId},
                ${Tables.StartedAuctions.columnOperationDate},
                ${Tables.StartedAuctions.columnTender},
                ${Tables.StartedAuctions.columnAuctions},
                ${Tables.StartedAuctions.columnBidders}
               )
               VALUES (?,?,?,?,?,?) IF NOT EXISTS
            """
    }

    private val preparedLoadStartedAuctionCQL = session.prepare(loadStartedAuctionCQL)
    private val preparedLoadAuctionsCQL = session.prepare(loadAuctionsCQL)
    private val preparedInsertStartedAuctionCQL = session.prepare(insertStartedAuctionCQL)

    override fun load(cpid: CPID): StartedAuctions? {
        val query = preparedLoadStartedAuctionCQL.bind().also {
            it.setString(RepositoryProperties.Tables.StartedAuctions.columnCpid, cpid)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when loading a record of the started auction with the cpid: '$cpid'  in the database.")
        }

        return resultSet.one()?.let { row -> rowToStartedAuctions(row) }
    }

    override fun loadAuctions(cpid: CPID): StartedAuctions.Auctions? {
        val query = preparedLoadAuctionsCQL.bind().also {
            it.setString(RepositoryProperties.Tables.StartedAuctions.columnCpid, cpid)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when loading a record of the started auction with the cpid: '$cpid'  in the database.")
        }

        return resultSet.one()?.let { row -> rowToAuctions(row) }
    }

    override fun save(cpid: CPID, operationId: OperationId, startedAuctions: StartedAuctions): StartedAuctions {
        val tender = objectMapper.toJson(startedAuctions.tender)
        val auctions = objectMapper.toJson(startedAuctions.auctions)
        val bidders = objectMapper.toJson(startedAuctions.bidders)

        val query = preparedInsertStartedAuctionCQL.bind().also {
            it.setString(Tables.StartedAuctions.columnCpid, cpid)
            it.setUUID(Tables.StartedAuctions.columnOperationId, operationId)
            it.setString(Tables.StartedAuctions.columnTender, tender)
            it.setString(Tables.StartedAuctions.columnAuctions, auctions)
            it.setString(Tables.StartedAuctions.columnBidders, bidders)
        }

        val resultSet = save(query)
        return if (resultSet.wasApplied())
            startedAuctions
        else
            rowToStartedAuctions(resultSet.one())
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

    private fun rowToTender(row: Row): StartedAuctions.Tender =
        objectMapper.toObject(row.getString(RepositoryProperties.Tables.StartedAuctions.columnTender))

    private fun rowToAuctions(row: Row): StartedAuctions.Auctions =
        objectMapper.toObject(row.getString(RepositoryProperties.Tables.StartedAuctions.columnAuctions))

    private fun rowToBidders(row: Row): StartedAuctions.Bidders =
        objectMapper.toObject(row.getString(RepositoryProperties.Tables.StartedAuctions.columnBidders))

    private fun rowToStartedAuctions(row: Row): StartedAuctions =
        StartedAuctions(tender = rowToTender(row),
            auctions = rowToAuctions(row),
            bidders = rowToBidders(row))
}