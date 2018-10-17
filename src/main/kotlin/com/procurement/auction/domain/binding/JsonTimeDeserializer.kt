package com.procurement.auction.domain.binding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class JsonTimeDeserializer : JsonDeserializer<LocalTime>() {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral('Z')
            .toFormatter()

        fun deserialize(dateTime: String): LocalTime = LocalTime.parse(dateTime, formatter)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LocalTime {
        val time = jsonParser.text
        return deserialize(time)
    }
}