package com.procurement.auction

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.cpid.CpidDeserializer
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.ocid.OcidDeserializer

abstract class AbstractBase {
    companion object {
        val mapper = ObjectMapper().apply {
            registerModule(getModule())

            this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        private fun getModule(): SimpleModule {
            val simpleModule = SimpleModule()
            simpleModule.addDeserializer(Cpid::class.java, CpidDeserializer())
            simpleModule.addDeserializer(Ocid::class.java, OcidDeserializer())
            return simpleModule
        }

        val RESOURCES = JsonResource()
    }
}
