package com.procurement.auction.configuration.properties

import com.procurement.auction.domain.KeyOfSlot
import com.procurement.auction.domain.binding.JsonTimeDeserializer
import com.procurement.auction.domain.schedule.SlotDefinition
import com.procurement.auction.service.DefaultSlotDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.util.*

class DefaultSchedulerProperties(schedulerProperties: SchedulerProperties) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultSchedulerProperties::class.java)
    }

    private val endTimeAllSlots: LocalTime by lazy {
        JsonTimeDeserializer.deserialize(
            schedulerProperties.endTimeAllSlots
                ?: throw IllegalStateException("Time of the end auctions not set.")
        )
    }

    private val qtyLinesPerSlot: Int by lazy {
        schedulerProperties.qtyLinesPerSlot
            ?: throw IllegalStateException("Not set qty lines per slot.")
    }

    val slotsDefinitions: TreeSet<SlotDefinition>

    init {
        val setSlotTimes = TreeSet<LocalTime>()
        val defaultsSlotsDefinitions = TreeSet<SlotDefinition>()
        var key: KeyOfSlot = GlobalProperties.Scheduler.keyOfFirstSlot
        val slotsDefinitions = getSlotsDefinitions(schedulerProperties)
        for (slotDefinition in slotsDefinitions) {
            val startTime = startTime(slotDefinition)
            if (!setSlotTimes.add(startTime))
                throw IllegalStateException("Times [$startTime] of the start slots is duplicate.")

            val endTime = endTime(slotDefinition) ?: endTimeAllSlots
            val maxLines = slotDefinition.maxLines ?: qtyLinesPerSlot
            defaultsSlotsDefinitions.add(
                DefaultSlotDefinition(
                    keyOfSlot = key,
                    startTime = startTime,
                    endTime = endTime,
                    maxLines = maxLines
                )
            )
            key++
        }
        this.slotsDefinitions = defaultsSlotsDefinitions

        log.info("Info of slots: ${slotsDefinitionsInfo(this.slotsDefinitions)}")
    }

    private fun getSlotsDefinitions(schedulerProperties: SchedulerProperties): List<SchedulerProperties.SlotDefinition> {
        val slotsDefinitions = schedulerProperties.slots!!
        if (slotsDefinitions.isEmpty()) throw IllegalStateException("No slot definition.")
        return slotsDefinitions
    }

    private fun startTime(slotDefinition: SchedulerProperties.SlotDefinition): LocalTime {
        val startTimeText = slotDefinition.startTime
            ?: throw IllegalStateException("Times of the start auctions in slots not set.")
        return JsonTimeDeserializer.deserialize(startTimeText)
    }

    private fun endTime(slotDefinition: SchedulerProperties.SlotDefinition): LocalTime? =
        slotDefinition.endTime?.let {
            JsonTimeDeserializer.deserialize(it)
        }

    private fun slotsDefinitionsInfo(set: TreeSet<SlotDefinition>): String {
        val sb = StringBuilder()
        sb.appendln("")
        for (detail in set) {
            sb.append("key: ")
            sb.append(detail.keyOfSlot)
            sb.append(", time range: [")
            sb.append(detail.startTime)
            sb.append(" - ")
            sb.append(detail.endTime)
            sb.append("], max lines: ")
            sb.appendln(detail.maxLines)
        }
        return sb.toString()
    }
}