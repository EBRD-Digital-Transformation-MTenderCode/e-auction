package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.auction.application.service.Transform
import com.procurement.auction.domain.extension.nowDefaultUTC
import com.procurement.auction.domain.extension.toDate
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.model.entity.HistoryEntity
import com.procurement.auction.domain.repository.HistoryRepository
import com.procurement.auction.infrastructure.extension.tryExecute
import org.springframework.stereotype.Repository

@Repository
class HistoryRepositoryCassandra(private val session: Session, private val transform: Transform) : HistoryRepository {

    companion object {
        private const val KEYSPACE = "auctions"
        private const val HISTORY_TABLE = "history"
        private const val OPERATION_ID = "operation_id"
        private const val COMMAND = "command"
        private const val COMMAND_DATE = "operation_date"
        const val JSON_DATA = "json_data"

        private const val SAVE_HISTORY_CQL = """
               INSERT INTO $KEYSPACE.$HISTORY_TABLE(
                      $OPERATION_ID,
                      $COMMAND,
                      $COMMAND_DATE,
                      $JSON_DATA
               )
               VALUES(?, ?, ?, ?)
               IF NOT EXISTS
            """

        private const val FIND_HISTORY_ENTRY_CQL = """
               SELECT $OPERATION_ID,
                      $COMMAND,
                      $COMMAND_DATE,
                      $JSON_DATA
                 FROM $KEYSPACE.$HISTORY_TABLE
                WHERE $OPERATION_ID=?
                  AND $COMMAND=?
               LIMIT 1
            """
    }

    private val preparedSaveHistoryCQL = session.prepare(SAVE_HISTORY_CQL)
    private val preparedFindHistoryByCpidAndCommandCQL = session.prepare(FIND_HISTORY_ENTRY_CQL)

    override fun getHistory(operationId: String, command: String): Result<HistoryEntity?, Fail.Incident> {
        val query = preparedFindHistoryByCpidAndCommandCQL.bind()
            .apply {
                setString(OPERATION_ID, operationId)
                setString(COMMAND, command)
            }

        return query.tryExecute(session)
            .doOnError { error -> return Result.failure(error) }
            .get
            .one()
            ?.let { row ->
                HistoryEntity(
                    row.getString(OPERATION_ID),
                    row.getString(COMMAND),
                    row.getTimestamp(COMMAND_DATE),
                    row.getString(JSON_DATA)
                )
            }
            .asSuccess()
    }

    override fun saveHistory(operationId: String, command: String, result: Any): Result<HistoryEntity, Fail.Incident> {
        val entity = HistoryEntity(
            operationId = operationId,
            command = command,
            operationDate = nowDefaultUTC().toDate(),
            jsonData = transform.trySerialization(result).orForwardFail { fail -> return fail }
        )

        val insert = preparedSaveHistoryCQL.bind()
            .apply {
                setString(OPERATION_ID, entity.operationId)
                setString(COMMAND, entity.command)
                setTimestamp(COMMAND_DATE, entity.operationDate)
                setString(JSON_DATA, entity.jsonData)
            }

        insert.tryExecute(session)
            .doOnError { error -> return Result.failure(error) }

        return entity.asSuccess()
    }
}
