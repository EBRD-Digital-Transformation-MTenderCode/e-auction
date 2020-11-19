package com.procurement.auction.infrastructure.bind

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.procurement.auction.domain.model.command.id.CommandIdModule
import com.procurement.auction.domain.model.command.name.CommandNameModule
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.cpid.CpidDeserializer
import com.procurement.auction.domain.model.date.JsonDateTimeModule
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.ocid.OcidDeserializer
import com.procurement.auction.domain.model.operationId.OperationIdModule
import com.procurement.auction.infrastructure.web.response.version.jackson.ApiVersionModule

fun ObjectMapper.configuration() {
    val module = SimpleModule().apply {
        addDeserializer(Cpid::class.java, CpidDeserializer())
        addDeserializer(Ocid::class.java, OcidDeserializer())
    }

    registerModule(module)
    registerModule(JsonDateTimeModule())
    registerModule(ApiVersionModule())
    registerModule(OperationIdModule())
    registerModule(CommandIdModule())
    registerModule(CommandNameModule())
    registerKotlinModule()

    configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
    configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
    configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)

    nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
}
