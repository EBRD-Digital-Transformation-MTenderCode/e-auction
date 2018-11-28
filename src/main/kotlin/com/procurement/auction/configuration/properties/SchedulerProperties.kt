package com.procurement.auction.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scheduler")
class SchedulerProperties {
    var endTimeAllSlots: String? = null
    var qtyLinesPerSlot: Int? = null
    var slots: MutableList<DefaultSlot>? = mutableListOf()

    class DefaultSlot(var startTime: String? = null,
                      var endTime: String? = null,
                      var maxLines: Int? = null)
}
