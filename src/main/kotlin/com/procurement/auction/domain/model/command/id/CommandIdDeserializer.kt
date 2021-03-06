package com.procurement.auction.domain.model.command.id

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class CommandIdDeserializer : ValueObjectDeserializer<CommandId>() {
    companion object {
        fun deserialize(text: String) = CommandId.tryCreateOrNull(text)
            ?: throw ParseCommandIdException(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): CommandId =
        deserialize(jsonParser.text)
}