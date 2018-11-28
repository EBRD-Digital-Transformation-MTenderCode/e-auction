package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.procurement.auction.domain.factory.TenderFactory
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.country.CountryDeserializer
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.cpid.CPIDSerializer
import com.procurement.auction.domain.model.operationId.OperationIdDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdSerializer
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.JsonSerializeService
import com.procurement.auction.domain.service.deserialize
import com.procurement.auction.exception.app.TenderIsAlreadyExistException
import com.procurement.auction.exception.database.OptimisticLockException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Repository

@Repository
class TenderRepositoryImpl(
    session: Session,
    private val tenderFactory: TenderFactory,
    private val serializer: JsonSerializeService,
    private val deserializer: JsonDeserializeService
) : AbstractRepository(session), TenderRepository {

    companion object {
        private val log: Logger = Slf4jLogger()

        private const val tableName = "auctions"
        private const val columnCpid = "cpid"
        private const val columnRowVersion = "row_version"
        private const val columnApiVersion = "api_version"
        private const val columnOperationId = "operation_id"
        private const val columnCountry = "country"
        private const val columnStatus = "status"
        private const val columnData = "data"
        private const val paramOriginalRowVersion = "originalRowVersion"

        private const val loadCQL =
            """SELECT $columnRowVersion,
                      $columnApiVersion,
                      $columnOperationId,
                      $columnCountry,
                      $columnStatus,
                      $columnData
                 FROM $KEY_SPACE.$tableName
                WHERE $columnCpid=?;"""

        private const val insertCQL =
            """INSERT INTO $KEY_SPACE.$tableName (
                           $columnCpid,
                           $columnRowVersion,
                           $columnApiVersion,
                           $columnOperationId,
                           $columnCountry,
                           $columnStatus,
                           $columnData
               )
               VALUES (?,?,?,?,?,?,?) IF NOT EXISTS;"""

        private const val updateCQL =
            """UPDATE $KEY_SPACE.$tableName
                  SET $columnRowVersion=?,
                      $columnOperationId=?,
                      $columnApiVersion=?,
                      $columnStatus=?,
                      $columnData=?
                WHERE $columnCpid=?
                   IF $columnRowVersion = :$paramOriginalRowVersion;
            """
    }

    private val preparedLoadCQL = session.prepare(loadCQL)
    private val preparedInsertCQL = session.prepare(insertCQL)
    private val preparedUpdateCQL = session.prepare(updateCQL)

    override fun load(cpid: CPID): Tender? {
        val query = preparedLoadCQL.bind().also {
            it.setString(columnCpid, CPIDSerializer.serialize(cpid))
        }

        val resultSet = load(query)
        return if (resultSet.wasApplied()) {
            resultSet.one()?.let { row ->
                val rowVersion = RowVersion.of(row.getInt(columnRowVersion))
                val operationId = OperationIdDeserializer.deserialize(row.getString(columnOperationId))
                val country = CountryDeserializer.deserialize(row.getString(columnCountry))
                val auctionsStatus = AuctionsStatus.valueOfId(row.getInt(columnStatus))
                val data = row.getString(columnData) ?: ""

                log.debug { "Read tender data: ($rowVersion, $operationId, auctions status: $auctionsStatus, data: '$data')" }

                tenderFactory.create(rowVersion = rowVersion,
                                     operationId = operationId,
                                     id = cpid,
                                     country = country,
                                     auctionsStatus = auctionsStatus,
                                     data = deserializer.deserialize(data)
                ).also {
                    log.debug { "The tender deserialize." }
                }
            }
        } else
            null
    }

    override fun saveScheduledAuctions(cpid: CPID, tender: Tender) {
        val snapshot = tender.toSnapshot()
        if (snapshot.data.tender.auctionsStatus != AuctionsStatus.SCHEDULED)
            throw IllegalStateException()

        isNeedSave(cpid, snapshot)

        val query =
            if (snapshot.rowVersion.isNew)
                prepareInsertQuery(cpid, snapshot)
            else
                prepareUpdateQuery(cpid, snapshot)

        val resultSet = save(query)
        if (!resultSet.wasApplied())
            throw TenderIsAlreadyExistException()
    }

    override fun saveCancelledAuctions(cpid: CPID, tender: Tender) {
        val snapshot = tender.toSnapshot()
        if (snapshot.data.tender.auctionsStatus != AuctionsStatus.CANCELED)
            throw IllegalStateException()

        isNeedSave(cpid, snapshot)

        val query = prepareUpdateQuery(cpid, snapshot)
        val resultSet = save(query)
        if (!resultSet.wasApplied()) {
            if (resultSet.columnDefinitions.contains(columnRowVersion)) {
                val row = resultSet.one()
                val versionInDatabase = row.getInt(columnRowVersion)
                if (versionInDatabase != snapshot.rowVersion.original)
                    throw OptimisticLockException(snapshot.rowVersion, versionInDatabase)
            }
            throw SaveOperationException(message = "Error writing cancelled auctions by tender with id: '$cpid' to the database. ${resultSetToString(
                resultSet)}")
        }
    }

    override fun saveStartedAuctions(cpid: CPID, tender: Tender) {
        val snapshot = tender.toSnapshot()
        if (snapshot.data.tender.auctionsStatus != AuctionsStatus.STARTED)
            throw IllegalStateException()

        isNeedSave(cpid, snapshot)

        val query = prepareUpdateQuery(cpid, snapshot)
        val resultSet = save(query)
        if (!resultSet.wasApplied()) {
            if (resultSet.columnDefinitions.contains(columnRowVersion)) {
                val row = resultSet.one()
                val versionInDatabase = row.getInt(columnRowVersion)
                if (versionInDatabase != snapshot.rowVersion.original)
                    throw OptimisticLockException(snapshot.rowVersion, versionInDatabase)
            }
            throw SaveOperationException(message = "Error writing started auctions by tender with id: '$cpid' to the database. ${resultSetToString(
                resultSet)}")
        }
    }

    override fun saveEndedAuctions(cpid: CPID, tender: Tender) {
        val snapshot = tender.toSnapshot()
        if (snapshot.data.tender.auctionsStatus != AuctionsStatus.ENDED)
            throw IllegalStateException()

        isNeedSave(cpid, snapshot)

        val query = prepareUpdateQuery(cpid, snapshot)
        val resultSet = save(query)
        if (!resultSet.wasApplied()) {
            if (resultSet.columnDefinitions.contains(columnRowVersion)) {
                val row = resultSet.one()
                val versionInDatabase = row.getInt(columnRowVersion)
                if (versionInDatabase != snapshot.rowVersion.original)
                    throw OptimisticLockException(snapshot.rowVersion, versionInDatabase)
            }
            throw SaveOperationException(message = "Error writing ended auctions by tender with id: '$cpid' to the database. ${resultSetToString(
                resultSet)}")
        }
    }

    private fun prepareInsertQuery(cpid: CPID, snapshot: TenderSnapshot): BoundStatement {
        val data: String = serializer.serialize(snapshot.data)
        val auctionsStatusId = snapshot.data.tender.auctionsStatus.id

        log.debug { "Attempt to save data on a new tender ($cpid, ${snapshot.rowVersion}, ${snapshot.apiVersion}, ${snapshot.operationId}, $data)" }

        return preparedInsertCQL.bind().also {
            it.setString(columnCpid, cpid.value)
            it.setInt(columnRowVersion, snapshot.rowVersion.modified)
            it.setString(columnApiVersion, ApiVersionSerializer.serialize(snapshot.data.apiVersion))
            it.setString(columnOperationId, OperationIdSerializer.serialize(snapshot.operationId))
            it.setString(columnCountry, CountrySerializer.serialize(snapshot.country))
            it.setInt(columnStatus, auctionsStatusId)
            it.setString(columnData, data)
        }
    }

    private fun prepareUpdateQuery(cpid: CPID, snapshot: TenderSnapshot): BoundStatement {
        val data: String = serializer.serialize(snapshot.data)
        val auctionsStatusId = snapshot.data.tender.auctionsStatus.id

        log.debug { "Attempt to update data of a tender ($cpid, ${snapshot.rowVersion}, ${snapshot.apiVersion}, ${snapshot.operationId}, $data)" }

        return preparedUpdateCQL.bind().also {
            it.setString(columnCpid, cpid.value)
            it.setInt(columnRowVersion, snapshot.rowVersion.modified)
            it.setString(columnApiVersion, ApiVersionSerializer.serialize(snapshot.data.apiVersion))
            it.setInt(columnStatus, auctionsStatusId)
            it.setString(columnOperationId, OperationIdSerializer.serialize(snapshot.operationId))
            it.setString(columnData, data)
            it.setInt(paramOriginalRowVersion, snapshot.rowVersion.original)
        }
    }

    private fun isNeedSave(cpid: CPID, tenderSnapshot: TenderSnapshot) {
        if (!tenderSnapshot.rowVersion.hasChanged)
            throw IllegalStateException("Data of auctions by cpid: '$cpid' was not saved, because the rowVersion was not changed.")
    }
}