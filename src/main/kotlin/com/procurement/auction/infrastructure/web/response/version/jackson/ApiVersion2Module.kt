package com.procurement.auction.infrastructure.web.response.version.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2

class ApiVersion2Module : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(
            ApiVersion2::class.java,
            ApiVersion2Serializer()
        )
        addDeserializer(
            ApiVersion2::class.java,
            ApiVersion2Deserializer()
        )
    }
}
