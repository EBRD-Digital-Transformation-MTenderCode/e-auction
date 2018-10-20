package com.procurement.auction.repository

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Session
import com.procurement.auction.cassandra.toCassandraLocalDate
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.KeyOfSlot
import com.procurement.auction.domain.Slots
import com.procurement.auction.exception.database.ReadOperationException
import com.procurement.auction.exception.database.SaveOperationException
import com.procurement.auction.repository.RepositoryProperties.KEY_SPACE
import com.procurement.auction.repository.RepositoryProperties.Tables
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

enum class SlotsSaveResult {
    OK, EXISTS, NOT_EXISTS
}

interface SlotsRepository {
    fun load(date: LocalDate, country: Country): Slots?
    fun save(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult
}

@Repository
class SlotsRepositoryImpl(private val session: Session) : SlotsRepository {
    companion object {
        private const val loadSlotsCQL =
            """SELECT
                ${Tables.Slots.columnSlot},
                ${Tables.Slots.columnStartTime},
                ${Tables.Slots.columnEndTime},
                ${Tables.Slots.columnMaxLine},
                ${Tables.Slots.columnCpids}
                 FROM $KEY_SPACE.${Tables.Slots.tableName}
                WHERE ${Tables.Slots.columnDate}=?
                  AND ${Tables.Slots.columnCountry}=?
            """

        private const val insertSlotsCQL =
            """INSERT INTO $KEY_SPACE.${Tables.Slots.tableName}
               (
                 ${Tables.Slots.columnDate},
                 ${Tables.Slots.columnCountry},
                 ${Tables.Slots.columnSlot},
                 ${Tables.Slots.columnStartTime},
                 ${Tables.Slots.columnEndTime},
                 ${Tables.Slots.columnMaxLine},
                 ${Tables.Slots.columnCpids}
               )
               VALUES (?,?,?,?,?,?,?) IF NOT EXISTS
            """

        private const val updateSlotsCQL =
            """UPDATE $KEY_SPACE.${Tables.Slots.tableName}
                  SET ${Tables.Slots.columnCpids}=${Tables.Slots.columnCpids} + :${Tables.Slots.paramCpid}
                WHERE ${Tables.Slots.columnDate}=?
                  AND ${Tables.Slots.columnCountry}=?
                  AND ${Tables.Slots.columnSlot}=?
                      IF EXISTS
            """
    }

    private val preparedLoadSlotsCQL = session.prepare(loadSlotsCQL)
    private val preparedInsertSlotsCQL = session.prepare(insertSlotsCQL)
    private val preparedUpdateSlotsCQL = session.prepare(updateSlotsCQL)

    override fun load(date: LocalDate, country: Country): Slots? {
        val query = preparedLoadSlotsCQL.bind().also {
            it.setDate(RepositoryProperties.Tables.Slots.columnDate, date.toCassandraLocalDate())
            it.setString(RepositoryProperties.Tables.Slots.columnCountry, country)
        }

        val resultSet = load(query)
        if (!resultSet.wasApplied()) {
            throw ReadOperationException(message = "An error occurred when loading a record with the day: '$date' in the database.")
        }

        val details = TreeSet<Slots.Definition>()
        for (row in resultSet) {
            val key = row.getInt(RepositoryProperties.Tables.Slots.columnSlot)

            details.add(
                Slots.Definition(
                    keyOfSlot = key,
                    startTime = LocalTime.ofNanoOfDay(row.getTime(RepositoryProperties.Tables.Slots.columnStartTime)),
                    endTime = LocalTime.ofNanoOfDay(row.getTime(RepositoryProperties.Tables.Slots.columnEndTime)),
                    maxLines = row.getInt(RepositoryProperties.Tables.Slots.columnMaxLine),
                    cpids = row.getSet(RepositoryProperties.Tables.Slots.columnCpids, String::class.javaObjectType)
                )
            )
        }

        return if (details.isNotEmpty())
            Slots(isNew = false, date = date, country = country, definitions = details)
        else
            null
    }

    override fun save(cpid: CPID, bookedSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult =
        if (slots.isNew) {
            val result = insert(cpid, bookedSlots, slots)
            if (result == SlotsSaveResult.EXISTS) {
                update(cpid, bookedSlots, slots)
            } else
                result
        } else
            update(cpid, bookedSlots, slots)

    private fun insert(cpid: CPID, bookSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult {
        val date = slots.date.toCassandraLocalDate()
        val country = slots.country
        val batch = BatchStatement()
        for (detail in slots.definitions) {
            val query = preparedInsertSlotsCQL.bind()
                .also {
                    it.setDate(RepositoryProperties.Tables.Slots.columnDate, date)
                    it.setString(RepositoryProperties.Tables.Slots.columnCountry, country)
                    it.setInt(RepositoryProperties.Tables.Slots.columnSlot, detail.keyOfSlot)
                    it.setTime(RepositoryProperties.Tables.Slots.columnStartTime, detail.startTime.toNanoOfDay())
                    it.setTime(RepositoryProperties.Tables.Slots.columnEndTime, detail.endTime.toNanoOfDay())
                    it.setInt(RepositoryProperties.Tables.Slots.columnMaxLine, detail.maxLines)

                    val cpids = if (bookSlots.contains(detail.keyOfSlot)) setOf(cpid) else emptySet()
                    it.setSet(RepositoryProperties.Tables.Slots.columnCpids, cpids)
                }
            batch.add(query)
        }

        val resultSet = save(batch)
        return if (!resultSet.wasApplied())
            SlotsSaveResult.EXISTS
        else
            SlotsSaveResult.OK
    }

    private fun update(cpid: CPID, bookSlots: Set<KeyOfSlot>, slots: Slots): SlotsSaveResult {
        val date = slots.date.toCassandraLocalDate()
        val country = slots.country
        val batch = BatchStatement()
        for (key in bookSlots) {
            val query = preparedUpdateSlotsCQL.bind()
                .also {
                    it.setDate(RepositoryProperties.Tables.Slots.columnDate, date)
                    it.setString(RepositoryProperties.Tables.Slots.columnCountry, country)
                    it.setInt(RepositoryProperties.Tables.Slots.columnSlot, key)
                    it.setSet(Tables.Slots.paramCpid, setOf(cpid))
                }
            batch.add(query)
        }

        val resultSet = save(batch)
        return if (!resultSet.wasApplied())
            SlotsSaveResult.EXISTS
        else
            SlotsSaveResult.OK
    }

    private fun load(statement: BoundStatement) = try {
        session.execute(statement)
    } catch (ex: Exception) {
        throw ReadOperationException(message = "Error read from the database.", cause = ex)
    }

    private fun save(statement: BatchStatement) = try {
        session.execute(statement)
    } catch (ex: Exception) {
        throw SaveOperationException(message = "Error writing to the database.", cause = ex)
    }
}