package com.procurement.auction.domain.model.slots.id

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

class SlotsIdsSerializer : JsonSerializer<Set<SlotId>>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(slotsIds: Set<SlotId>, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeStartArray()
        slotsIds.forEach { slotId ->
            jsonGenerator.writeString(SlotIdSerializer.serialize(
                slotId))
        }
        jsonGenerator.writeEndArray()
    }
}