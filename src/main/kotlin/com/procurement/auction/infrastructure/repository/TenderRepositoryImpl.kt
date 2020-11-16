package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.operationId.OperationIdDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdSerializer
import com.procurement.auction.domain.model.tender.TenderEntity
import com.procurement.auction.domain.model.tender.snapshot.CancelledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.EndedAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot
import com.procurement.auction.domain.model.version.ApiVersion
import com.procurement.auction.domain.model.version.ApiVersionSerializer
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.TenderRepository
import com.procurement.auction.domain.service.JsonSerializeService
import com.procurement.auction.exception.app.TenderIsAlreadyExistException
import com.procurement.auction.exception.database.OptimisticLockException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Repository

@Repository
class TenderRepositoryImpl(
    session: Session,
    private val serializer: JsonSerializeService
) : AbstractRepository(session), TenderRepository {

    companion object {
        private val log: Logger = Slf4jLogger()

        private const val tableName = "auctions"
        private const val columnCpid = "cpid"
        private const val columnOcid = "ocid"
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
                      $columnStatus,
                      $columnData
                 FROM $KEY_SPACE.$tableName
                WHERE $columnCpid=?
                  AND $columnOcid=?;"""

        private const val insertCQL =
            """INSERT INTO $KEY_SPACE.$tableName (
                           $columnCpid,
                           $columnOcid,
                           $columnRowVersion,
                           $columnApiVersion,
                           $columnOperationId,
                           $columnCountry,
                           $columnStatus,
                           $columnData
               )
               VALUES (?,?,?,?,?,?,?,?) IF NOT EXISTS;"""

        private const val updateCQL =
            """UPDATE $KEY_SPACE.$tableName
                  SET $columnRowVersion=?,
                      $columnOperationId=?,
                      $columnApiVersion=?,
                      $columnStatus=?,
                      $columnData=?
                WHERE $columnCpid=?
                  AND $columnOcid=?
                   IF $columnRowVersion = :$paramOriginalRowVersion;
            """
    }

    private val preparedLoadCQL = session.prepare(loadCQL)
    private val preparedInsertCQL = session.prepare(insertCQL)
    private val preparedUpdateCQL = session.prepare(updateCQL)

    override fun loadEntity(cpid: Cpid, ocid: Ocid): TenderEntity? {
        val query = preparedLoadCQL.bind().apply {
            setString(columnCpid, cpid.toString())
            setString(columnOcid, ocid.toString())
        }

        val resultSet = load(query)
        return if (resultSet.wasApplied()) {
            resultSet.one()?.let { row ->
                val rowVersion = RowVersion.of(row.getInt(columnRowVersion))
                val operationId = OperationIdDeserializer.deserialize(row.getString(columnOperationId))
                val status = AuctionsStatus.valueOfId(row.getInt(columnStatus))
                val data = row.getString(columnData)

                log.debug { "Read tender data: ($rowVersion, $operationId, auctions status: ${status.description}, data: '$data')" }
                TenderEntity(
                    rowVersion = rowVersion,
                    operationId = operationId,
                    status = status,
                    cpid = cpid,
                    ocid = ocid,
                    data = data
                )
            }
        } else
            null
    }

    override fun save(snapshot: ScheduledAuctionsSnapshot) {
        if (snapshot.data.tender.status != AuctionsStatus.SCHEDULED)
            throw IllegalStateException()

        val query =
            if (snapshot.rowVersion.isNew) {
                val data = serializer.serialize(snapshot.data)
                prepareInsertQuery(
                    cpid = snapshot.cpid,
                    ocid = snapshot.ocid,
                    rowVersion = snapshot.rowVersion,
                    apiVersion = snapshot.data.apiVersion,
                    operationId = snapshot.operationId,
                    country = snapshot.data.tender.country,
                    status = snapshot.data.tender.status,
                    data = data
                )
            } else {
                isNeedSave(snapshot.data.tender.id, snapshot.rowVersion)
                val data = serializer.serialize(snapshot.data)
                prepareUpdateQuery(
                    cpid = snapshot.cpid,
                    ocid = snapshot.ocid,
                    rowVersion = snapshot.rowVersion,
                    apiVersion = snapshot.data.apiVersion,
                    operationId = snapshot.operationId,
                    status = snapshot.data.tender.status,
                    data = data
                )
            }

        val resultSet = save(query)
        if (!resultSet.wasApplied())
            throw TenderIsAlreadyExistException()
    }

    override fun save(snapshot: CancelledAuctionsSnapshot) {
        if (snapshot.data.tender.status != AuctionsStatus.CANCELED)
            throw IllegalStateException()

        isNeedSave(snapshot.data.tender.id, snapshot.rowVersion)

        val data = serializer.serialize(snapshot.data)
        save(
            cpid = snapshot.cpid,
            ocid = snapshot.ocid,
            rowVersion = snapshot.rowVersion,
            apiVersion = snapshot.data.apiVersion,
            operationId = snapshot.operationId,
            status = snapshot.data.tender.status,
            data = data
        )
    }

    override fun save(snapshot: StartedAuctionsSnapshot) {
        if (snapshot.data.tender.status != AuctionsStatus.STARTED)
            throw IllegalStateException()

        isNeedSave(snapshot.data.tender.id, snapshot.rowVersion)

        val data = serializer.serialize(snapshot.data)
        save(
            cpid = snapshot.cpid,
            ocid = snapshot.ocid,
            rowVersion = snapshot.rowVersion,
            apiVersion = snapshot.data.apiVersion,
            operationId = snapshot.operationId,
            status = snapshot.data.tender.status,
            data = data
        )
    }

    override fun save(snapshot: EndedAuctionsSnapshot) {
        if (snapshot.data.tender.status != AuctionsStatus.ENDED)
            throw IllegalStateException()

        isNeedSave(snapshot.data.tender.id, snapshot.rowVersion)

        val data = serializer.serialize(snapshot.data)
        save(
            cpid = snapshot.cpid,
            ocid = snapshot.ocid,
            rowVersion = snapshot.rowVersion,
            apiVersion = snapshot.data.apiVersion,
            operationId = snapshot.operationId,
            status = snapshot.data.tender.status,
            data = data
        )
    }

    private fun save(cpid: Cpid,
                     ocid: Ocid,
                     rowVersion: RowVersion,
                     apiVersion: ApiVersion,
                     operationId: OperationId,
                     status: AuctionsStatus,
                     data: String) {

        val query = prepareUpdateQuery(
            cpid = cpid,
            ocid = ocid,
            rowVersion = rowVersion,
            apiVersion = apiVersion,
            operationId = operationId,
            status = status,
            data = data
        )

        val resultSet = save(query)
        if (!resultSet.wasApplied()) {
            if (resultSet.columnDefinitions.contains(columnRowVersion)) {
                val row = resultSet.one()
                val versionInDatabase = row.getInt(columnRowVersion)
                if (versionInDatabase != rowVersion.original)
                    throw OptimisticLockException(rowVersion, versionInDatabase)
            }

            val resultSetOfString = resultSetToString(resultSet)
            throw SaveOperationException(
                message = "Error writing tender with id: '$cpid' in status: '${status.description}' to the database. $resultSetOfString"
            )
        }
    }

    private fun prepareInsertQuery(cpid: Cpid,
                                   ocid: Ocid,
                                   rowVersion: RowVersion,
                                   apiVersion: ApiVersion,
                                   operationId: OperationId,
                                   country: Country,
                                   status: AuctionsStatus,
                                   data: String): BoundStatement {

        log.debug { "Attempt to save data on a new tender ($cpid, $rowVersion, $apiVersion, $operationId, $data)" }

        return preparedInsertCQL.bind().apply {
            setString(columnCpid, cpid.toString())
            setString(columnOcid, ocid.toString())
            setInt(columnRowVersion, rowVersion.modified)
            setString(columnApiVersion, ApiVersionSerializer.serialize(apiVersion))
            setString(columnOperationId, OperationIdSerializer.serialize(operationId))
            setString(columnCountry, CountrySerializer.serialize(country))
            setInt(columnStatus, status.id)
            setString(columnData, data)
        }
    }

    private fun prepareUpdateQuery(cpid: Cpid,
                                   ocid: Ocid,
                                   rowVersion: RowVersion,
                                   apiVersion: ApiVersion,
                                   operationId: OperationId,
                                   status: AuctionsStatus,
                                   data: String): BoundStatement {

        log.debug { "Attempt to update data of a tender ($cpid, $rowVersion, $apiVersion, $operationId, $data)" }

        return preparedUpdateCQL.bind().apply {
            setString(columnCpid, cpid.toString())
            setString(columnOcid, ocid.toString())
            setInt(columnRowVersion, rowVersion.modified)
            setString(columnApiVersion, ApiVersionSerializer.serialize(apiVersion))
            setString(columnOperationId, OperationIdSerializer.serialize(operationId))
            setInt(columnStatus, status.id)
            setString(columnData, data)
            setInt(paramOriginalRowVersion, rowVersion.original)
        }
    }

    private fun isNeedSave(cpid: Cpid, rowVersion: RowVersion) {
        if (!rowVersion.hasChanged)
            throw IllegalStateException("Data of auctions of tender with id: '$cpid' was not saved, because the rowVersion was not changed.")
    }
}