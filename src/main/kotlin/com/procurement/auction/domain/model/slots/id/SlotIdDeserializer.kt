package com.procurement.auction.domain.model.slots.id

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException
import java.util.*

class SlotIdDeserializer : ValueObjectDeserializer<SlotId>() {
    companion object {
        fun deserialize(text: String) = SlotId(UUID.fromString(text))
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): SlotId =
        deserialize(jsonParser.text)
}