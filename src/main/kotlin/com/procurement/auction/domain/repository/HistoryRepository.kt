package com.procurement.auction.domain.repository

import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.model.entity.HistoryEntity

interface HistoryRepository {
    fun getHistory(operationId: String, command: String): Result<HistoryEntity?, Fail.Incident>
    fun saveHistory(operationId: String, command: String, result: Any): Result<HistoryEntity, Fail.Incident>
}
