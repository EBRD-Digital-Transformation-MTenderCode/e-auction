package com.procurement.auction.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.auction.application.service.Transform
import com.procurement.auction.domain.extension.nowDefaultUTC
import com.procurement.auction.domain.extension.toDate
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.asSuccess
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.domain.repository.HistoryRepository
import com.procurement.auction.infrastructure.extension.tryExecute
import com.procurement.auction.infrastructure.service.command.type.Action
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

    override fun getHistory(commandId: CommandId, action: Action): Result<String?, Fail.Incident> {
        val query = preparedFindHistoryByCpidAndCommandCQL.bind()
            .apply {
                setString(OPERATION_ID, commandId.underlying)
                setString(COMMAND, action.key)
            }

        return query.tryExecute(session)
            .doOnError { error -> return Result.failure(error) }
            .get
            .one()
            ?.getString(JSON_DATA)
            .asSuccess()
    }

    override fun saveHistory(commandId: CommandId, action: Action, result: Any): Result<Boolean, Fail.Incident> {
        val data = transform.trySerialization(result).orForwardFail { return it }
        val insert = preparedSaveHistoryCQL.bind()
            .apply {
                setString(OPERATION_ID, commandId.underlying)
                setString(COMMAND, action.key)
                setTimestamp(COMMAND_DATE, nowDefaultUTC().toDate())
                setString(JSON_DATA, data)
            }

        return insert.tryExecute(session)
            .doReturn { error -> return Result.failure(error) }
            .wasApplied()
            .asSuccess()
    }
}
