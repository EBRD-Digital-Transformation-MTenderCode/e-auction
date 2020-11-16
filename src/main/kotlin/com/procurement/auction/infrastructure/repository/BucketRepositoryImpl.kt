package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.auction.domain.factory.BucketFactory
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.model.bucket.Bucket
import com.procurement.auction.domain.model.bucket.BucketSnapshot
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.country.CountrySerializer
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.BucketRepository
import com.procurement.auction.domain.service.JsonSerializeService
import com.procurement.auction.exception.BucketIsAlreadyExistException
import com.procurement.auction.exception.database.OptimisticLockException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.infrastructure.cassandra.toCassandraLocalDate
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import com.procurement.auction.infrastructure.web.response.version.jackson.ApiVersion2Deserializer
import com.procurement.auction.infrastructure.web.response.version.jackson.ApiVersion2Serializer
import org.springframework.stereotype.Repository

@Repository
class BucketRepositoryImpl(
    session: Session,
    private val bucketFactory: BucketFactory,
    private val serializer: JsonSerializeService
) : AbstractRepository(session), BucketRepository {
    companion object {
        private val log: Logger = Slf4jLogger()

        private const val tableName = "buckets"
        private const val columnDate = "date"
        private const val columnCountry = "country"
        private const val columnRowVersion = "row_version"
        private const val columnApiVersion = "api_version"
        private const val columnSlots = "slots"
        private const val columnOccupancy = "occupancy"
        private const val paramOriginalRowVersion = "originalRowVersion"

        private const val loadCQL =
            """SELECT $columnRowVersion,
                      $columnApiVersion,
                      $columnSlots,
                      $columnOccupancy
                 FROM $KEY_SPACE.$tableName
                WHERE $columnDate=?
                  AND $columnCountry=?;
            """

        private const val insertCQL =
            """INSERT INTO $KEY_SPACE.$tableName
               (
                 $columnDate,
                 $columnCountry,
                 $columnRowVersion,
                 $columnApiVersion,
                 $columnSlots,
                 $columnOccupancy
               )
               VALUES (?,?,?,?,?,?) IF NOT EXISTS;
            """

        private const val updateCQL =
            """UPDATE $KEY_SPACE.$tableName
                  SET $columnOccupancy=?,
                      $columnRowVersion=?
                WHERE $columnDate=?
                  AND $columnCountry=?
                   IF $columnRowVersion=:$paramOriginalRowVersion;
            """
    }

    private val preparedLoadCQL = session.prepare(loadCQL)
    private val preparedInsertCQL = session.prepare(insertCQL)
    private val preparedUpdateCQL = session.prepare(updateCQL)

    override fun load(bucketId: BucketId): Bucket? {
        val query = preparedLoadCQL.bind().also {
            it.setDate(columnDate, bucketId.date.toCassandraLocalDate())
            it.setString(columnCountry, CountrySerializer.serialize(bucketId.country))
        }

        val resultSet = load(query)
        return if (resultSet.wasApplied()) {
            resultSet.one()?.let { row ->
                val rowVersion = RowVersion.of(row.getInt(columnRowVersion))
                val apiVersion: ApiVersion = ApiVersion2Deserializer.deserialize(row.getString(columnApiVersion))
                val slots: String = row.getString(columnSlots)
                val occupancy: String = row.getString(columnOccupancy)

                log.debug { "Read bucket data: ($rowVersion, $apiVersion, slots: '$slots', occupancy: '$occupancy')" }

                bucketFactory.create(
                    id = bucketId,
                    rowVersion = rowVersion,
                    apiVersion = apiVersion,
                    slots = slots,
                    occupancy = occupancy
                ).also {
                    log.debug { "Deserialize bucket: ($it)" }
                }
            }
        } else
            null
    }

    override fun save(bucket: Bucket) {
        val bucketSnapshot: BucketSnapshot = bucket.toSnapshot()
        if (bucketSnapshot.rowVersion.isNew)
            insert(bucketSnapshot)
        else
            update(bucketSnapshot)
    }

    private fun insert(snapshot: BucketSnapshot) {
        isNeedSave(snapshot)

        val slots = serializer.serialize(snapshot.slots)
        val occupancy = serializer.serialize(snapshot.occupancy)
        val query = preparedInsertCQL.bind().also {
            it.setDate(columnDate, snapshot.id.date.toCassandraLocalDate())
            it.setString(columnCountry, CountrySerializer.serialize(snapshot.id.country))
            it.setInt(columnRowVersion, snapshot.rowVersion.modified)
            it.setString(columnApiVersion, ApiVersion2Serializer.serialize(snapshot.apiVersion))
            it.setString(columnSlots, slots)
            it.setString(columnOccupancy, occupancy)
        }

        val resultSet = save(query)
        if (!resultSet.wasApplied())
            throw BucketIsAlreadyExistException("Bucket were created another client.")
    }

    private fun update(snapshot: BucketSnapshot) {
        isNeedSave(snapshot)

        val occupancy = serializer.serialize(snapshot.occupancy)
        val query = preparedUpdateCQL.bind().also {
            it.setDate(columnDate, snapshot.id.date.toCassandraLocalDate())
            it.setString(columnCountry, CountrySerializer.serialize(snapshot.id.country))
            it.setInt(columnRowVersion, snapshot.rowVersion.modified)
            it.setString(columnOccupancy, occupancy)
            it.setInt(paramOriginalRowVersion, snapshot.rowVersion.original)
        }

        val resultSet = save(query)
        if (!resultSet.wasApplied()) {
            if (resultSet.columnDefinitions.contains(columnRowVersion)) {
                val row = resultSet.one()
                val versionInDatabase = row.getInt(columnRowVersion)
                if (versionInDatabase != snapshot.rowVersion.original)
                    throw OptimisticLockException(
                        readVersion = snapshot.rowVersion,
                        versionInDatabase = versionInDatabase
                    )
            }

            throw SaveOperationException(message = "Error writing bucket to the database. ${resultSetToString(resultSet)}")
        }
    }

    private fun isNeedSave(snapshot: BucketSnapshot) {
        if (!snapshot.rowVersion.hasChanged)
            throw IllegalStateException("Data of bucket by id: '${snapshot.id}' was not saved, because the rowVersion was not changed.")
    }
}