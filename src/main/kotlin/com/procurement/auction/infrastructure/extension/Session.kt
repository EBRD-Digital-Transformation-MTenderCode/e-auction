package com.procurement.auction.infrastructure.extension

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.Result
import com.procurement.auction.domain.functional.Result.Companion.failure
import com.procurement.auction.domain.functional.Result.Companion.success

fun BoundStatement.tryExecute(session: Session): Result<ResultSet, Fail.Incident.Database.Interaction> = try {
    success(session.execute(this))
} catch (expected: Exception) {
    failure(Fail.Incident.Database.Interaction(exception = expected))
}

fun BatchStatement.tryExecute(session: Session): Result<ResultSet, Fail.Incident.Database.Interaction> = try {
    success(session.execute(this))
} catch (expected: Exception) {
    failure(Fail.Incident.Database.Interaction(exception = expected))
}
