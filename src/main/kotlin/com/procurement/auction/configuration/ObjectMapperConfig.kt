package com.procurement.auction.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.procurement.auction.domain.model.date.JsonDateTimeDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeSerializer
import com.procurement.auction.infrastructure.web.response.version.jackson.ApiVersion2Module
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime


@Configuration
class ObjectMapperConfig(@Autowired objectMapper: ObjectMapper) {

    init {
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java, JsonDateTimeSerializer())
        module.addDeserializer(LocalDateTime::class.java, JsonDateTimeDeserializer())
        objectMapper.registerModule(module)
        objectMapper.registerModule(ApiVersion2Module())
        objectMapper.registerKotlinModule()
        objectMapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
    }
}
