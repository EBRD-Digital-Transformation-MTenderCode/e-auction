package com.procurement.auction.domain.repository

import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.service.command.type.Action

interface HistoryRepository {
    fun getHistory(commandId: CommandId, action: Action): Result<String?, Fail.Incident>
    fun saveHistory(commandId: CommandId, action: Action, result: Any): Result<Boolean, Fail.Incident>
}
