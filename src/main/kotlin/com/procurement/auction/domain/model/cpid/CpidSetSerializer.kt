package com.procurement.auction.domain.model.cpid

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

class CpidSetSerializer : JsonSerializer<Set<CPID>>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(cpids: Set<CPID>, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeStartArray()
        cpids.forEach { cpid ->
            jsonGenerator.writeString(CPIDSerializer.serialize(cpid))
        }
        jsonGenerator.writeEndArray()
    }
}