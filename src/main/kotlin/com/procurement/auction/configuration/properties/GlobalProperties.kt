package com.procurement.auction.configuration.properties

import com.procurement.auction.domain.ApiVersion

object GlobalProperties {
    const val serviceId = "15"

    object App {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    object Auction {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    object Scheduler {
        const val keyOfFirstSlot = 1
    }
}

