package com.procurement.auction.domain.model.cpid

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class CpidSetDeserializer : JsonDeserializer<Set<CPID>>() {
    companion object {
        val cpidSetType: TypeReference<List<String>> = object : TypeReference<List<String>>() {}
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Set<CPID> {
        val simpleSet: List<String> = jsonParser.readValueAs(CpidSetDeserializer.cpidSetType)
        val result = mutableSetOf<CPID>()
        for (cpid in simpleSet) {
            result.add(CPIDDeserializer.deserialize(cpid))
        }
        return result
    }
}