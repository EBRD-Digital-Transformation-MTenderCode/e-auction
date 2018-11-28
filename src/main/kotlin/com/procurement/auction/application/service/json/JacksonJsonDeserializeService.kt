package com.procurement.auction.application.service.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.auction.domain.service.JsonDeserializeService
import com.procurement.auction.exception.json.JsonParseToObjectException
import org.springframework.stereotype.Service

@Service
class JacksonJsonDeserializeService(private val objectMapper: ObjectMapper) : JsonDeserializeService {
    override fun <T> deserialize(json: String, targetClass: Class<T>): T = try {
        objectMapper.readValue(json, targetClass)
    } catch (exception: Exception) {
        throw JsonParseToObjectException(json, exception)
    }
}