package com.procurement.auction.domain.model.lotId

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

class LotsIdsSerializer : JsonSerializer<List<LotId>>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(lotIds: List<LotId>, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeStartArray()
        lotIds.forEach { lotId ->
            jsonGenerator.writeString(LotIdSerializer.serialize(lotId))
        }
        jsonGenerator.writeEndArray()
    }
}