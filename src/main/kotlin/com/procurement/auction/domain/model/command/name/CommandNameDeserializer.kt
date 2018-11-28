package com.procurement.auction.domain.model.command.name

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.procurement.auction.domain.model.ValueObjectDeserializer
import java.io.IOException

class CommandNameDeserializer : ValueObjectDeserializer<CommandName>() {
    companion object {
        fun deserialize(text: String) = CommandName.valueOfCode(text)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): CommandName =
        deserialize(jsonParser.text)
}