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

        object ScheduledAuctions {
            const val tableName = "auction_schedule"
            const val columnCpid = "cpid"
            const val columnOperationId = "operation_id"
            const val columnOperationDate = "operation_date"
            const val columnAuctions = "auctions"
        }

        object StartedAuctions {
            const val tableName = "auction_start"
            const val columnCpid = "cpid"
            const val columnOperationId = "operation_id"
            const val columnOperationDate = "operation_date"
            const val columnTender = "tender"
            const val columnAuctions = "auctions"
            const val columnBidders = "bidders"
        }

        object EndedAuctions {
            const val tableName = "auction_end"
            const val columnCpid = "cpid"
            const val columnOperationId = "operation_id"
            const val columnOperationDate = "operation_date"
            const val columnTender = "tender"
            const val columnAuctions = "auctions"
        }
    }
}
