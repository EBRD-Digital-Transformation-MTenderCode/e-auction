package com.procurement.auction.infrastructure.cassandra

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDate.toCassandraLocalDate(): com.datastax.driver.core.LocalDate =
    com.datastax.driver.core.LocalDate.fromDaysSinceEpoch(this.toEpochDay().toInt())

fun Date.toLocalDateTime(): LocalDateTime =
    this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
