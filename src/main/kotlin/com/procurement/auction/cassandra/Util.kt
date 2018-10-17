package com.procurement.auction.cassandra

import java.time.LocalDate

fun LocalDate.toCassandraLocalDate(): com.datastax.driver.core.LocalDate =
    com.datastax.driver.core.LocalDate.fromDaysSinceEpoch(this.toEpochDay().toInt())

