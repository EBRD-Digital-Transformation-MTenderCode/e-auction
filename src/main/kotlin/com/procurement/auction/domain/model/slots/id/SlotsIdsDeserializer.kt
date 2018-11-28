package com.procurement.auction.domain.model.slots.id

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class SlotsIdsDeserializer : JsonDeserializer<Set<SlotId>>() {
    companion object {
        val relatedLotsType: TypeReference<List<String>> = object : TypeReference<List<String>>() {}
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Set<SlotId> {
        val simpleList: List<String> = jsonParser.readValueAs(relatedLotsType)
        val result = mutableSetOf<SlotId>()
        for (i in 0 until simpleList.size) {
            val value = simpleList[i]
            result.add(SlotIdDeserializer.deserialize(value))
        }
        return result
    }
}