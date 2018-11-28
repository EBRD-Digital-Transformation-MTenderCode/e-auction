package com.procurement.auction.domain.model.operationId

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException
import java.util.*

class OperationIdDeserializer : ValueObjectDeserializer<OperationId>() {
    companion object {
        fun deserialize(text: String) = OperationId(UUID.fromString(text))
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): OperationId =
        OperationIdDeserializer.deserialize(jsonParser.text)
}