package com.procurement.auction

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

abstract class AbstractBase {
    companion object {
        val mapper = ObjectMapper().apply {
            this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val RESOURCES = JsonResource()
    }
}
