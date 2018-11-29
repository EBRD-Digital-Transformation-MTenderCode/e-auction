package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.cpid.CPIDSerializer
import com.procurement.auction.domain.model.migration.OldAuctions
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.repository.TenderMigrationRepository
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.domain.service.deserialize
import com.procurement.auction.infrastructure.cassandra.toLocalDateTime
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class TenderMigrationRepositoryImpl(
    session: Session,
    private val deserializer: JsonDeserializeService
) : AbstractRepository(session), TenderMigrationRepository {

    companion object {
        private const val oldTableName = "auction_planning"
        private const val oldColumnId = "cpid"
        private const val oldColumnOperationId = "operation_id"
        private const val oldColumnOperationDate = "operation_date"
        private const val oldColumnData = "data"

        private const val loadCQL =
            """SELECT $oldColumnOperationId,
                      $oldColumnOperationDate,
                      $oldColumnData
                 FROM ocds.$oldTableName
                WHERE $oldColumnId=?;"""
    }

    private val preparedLoadCQL = session.prepare(loadCQL)

    override fun load(cpid: CPID): List<OldAuctions> {
        val query = preparedLoadCQL.bind().also {
            it.setString(oldColumnId, CPIDSerializer.serialize(cpid))
        }

        val resultSet = load(query)
        return if (resultSet.wasApplied()) {
            val result = mutableListOf<OldAuctions>()

            for (row in resultSet.all()) {
                val operationId: OperationId = OperationId(row.getUUID(oldColumnOperationId))
                val operationDate: LocalDateTime = row.getTimestamp(oldColumnOperationDate).toLocalDateTime()
                val data: OldAuctions.Data = deserializer.deserialize(row.getString(oldColumnData))

                result.add(
                    OldAuctions(
                        cpid = cpid,
                        operationId = operationId,
                        operationDate = operationDate,
                        data = data
                    )
                )
            }
            return result
        } else
            emptyList()
    }
}