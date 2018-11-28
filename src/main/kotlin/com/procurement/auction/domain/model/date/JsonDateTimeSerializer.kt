package com.procurement.auction.domain.model.date

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class JsonDateTimeSerializer : JsonSerializer<LocalDateTime>() {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral('Z')
            .toFormatter()

        fun serialize(data: LocalDateTime): String = data.format(formatter)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(date: LocalDateTime, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(serialize(date))
    }
}