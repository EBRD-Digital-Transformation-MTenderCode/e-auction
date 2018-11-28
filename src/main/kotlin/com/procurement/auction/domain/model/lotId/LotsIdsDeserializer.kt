package com.procurement.auction.domain.model.lotId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class LotsIdsDeserializer : JsonDeserializer<List<LotId>>() {
    companion object {
        val relatedLotsType: TypeReference<List<String>> = object : TypeReference<List<String>>() {}
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): List<LotId> {
        val simpleList: List<String> = jsonParser.readValueAs(relatedLotsType)
        val result = mutableListOf<LotId>()
        for (i in 0 until simpleList.size) {
            val value = simpleList[i]
            result.add(LotIdDeserializer.deserialize(value))
        }
        return result
    }
}