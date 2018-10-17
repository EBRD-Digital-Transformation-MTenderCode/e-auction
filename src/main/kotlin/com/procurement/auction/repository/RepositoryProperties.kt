package com.procurement.auction.repository

object RepositoryProperties {
    const val KEY_SPACE = "ocds"

    object Tables {
        object Calendar {
            const val tableName = "auction_calendar"
            const val columnCountry = "country"
            const val columnYear = "year"
            const val columnMonth = "month"
            const val columnWorkDays = "work_days"
        }

        object AuctionPlanning {
            const val tableName = "auction_planning"
            const val columnCpid = "cpid"
            const val columnOperationId = "operation_id"
            const val columnOperationDate = "operation_date"
            const val columnData = "data"
        }

        object Slots {
            const val tableName = "auction_slots"
            const val columnDate = "date"
            const val columnCountry = "country"
            const val columnSlot = "slot"
            const val columnStartTime = "start_time"
            const val columnEndTime = "end_time"
            const val columnMaxLine = "max_lines"
            const val columnCpids = "cpids"
            const val paramCpid = "newCpid"
        }
    }
}
