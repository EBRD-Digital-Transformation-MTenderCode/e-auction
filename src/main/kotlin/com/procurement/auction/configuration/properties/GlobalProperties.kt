package com.procurement.auction.configuration.properties

import com.procurement.auction.domain.ApiVersion

object GlobalProperties {
    const val serviceId = "15"

    object App {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    object AuctionSchedule {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    object AuctionStart {
        val apiVersion = ApiVersion(1, 0, 0)
    }


    object AuctionEnd {
        val apiVersion = ApiVersion(1, 0, 0)
    }

    object Scheduler {
        const val keyOfFirstSlot = 1
    }
}

