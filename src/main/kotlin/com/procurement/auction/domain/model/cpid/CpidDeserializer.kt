package com.procurement.auction.domain.model.cpid

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.auction.domain.model.Cpid

class CpidDeserializer : JsonDeserializer<Cpid>() {
    companion object {
        fun deserialize(cpid: String): Cpid =
            Cpid.tryCreateOrNull(cpid) ?: throw ParseCpidException(cpid)
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Cpid =
        deserialize(jsonParser.text)
}