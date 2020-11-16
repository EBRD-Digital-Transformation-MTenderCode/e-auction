package com.procurement.auction.domain.model.operationId

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2

class OperationIdModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(OperationId::class.java, OperationIdSerializer())
        addDeserializer(OperationId::class.java, OperationIdDeserializer())
    }
}
