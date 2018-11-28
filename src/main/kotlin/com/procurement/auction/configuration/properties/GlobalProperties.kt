package com.procurement.auction.configuration.properties

import com.procurement.auction.domain.model.version.ApiVersion

object GlobalProperties {
    const val serviceId = "15"

    object App {
        val apiVersion = ApiVersion(1, 0, 0)
    }
}

