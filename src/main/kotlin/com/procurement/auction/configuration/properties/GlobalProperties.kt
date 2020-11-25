package com.procurement.auction.configuration.properties

import com.procurement.auction.infrastructure.web.response.version.ApiVersion

object GlobalProperties {
    const val serviceId = "15"

    object App {
        val apiVersion = ApiVersion(1, 0, 0)
    }
}

