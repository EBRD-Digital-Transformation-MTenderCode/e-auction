package com.procurement.auction.domain.model.slots.id

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.auction.domain.model.ValueObjectSerializer
import java.io.IOException

class SlotIdSerializer : ValueObjectSerializer<SlotId>() {
    companion object {
        fun serialize(slotId: SlotId) = slotId.value.toString()
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(slotId: SlotId, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(
            slotId))
}