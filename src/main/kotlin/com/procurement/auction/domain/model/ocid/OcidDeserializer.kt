package com.procurement.auction.domain.model.ocid

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.auction.domain.model.Ocid

class OcidDeserializer : JsonDeserializer<Ocid>() {
    companion object {
        fun deserialize(ocid: String): Ocid =
            Ocid.tryCreateOrNull(ocid) ?: throw ParseOcidException(ocid)
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Ocid =
        deserialize(jsonParser.text)
}