package com.procurement.auction.infrastructure.web.response.version.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.auction.infrastructure.web.response.version.ApiVersion

class ApiVersionModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(ApiVersion::class.java, ApiVersionSerializer())
        addDeserializer(ApiVersion::class.java, ApiVersionDeserializer())
    }
}
