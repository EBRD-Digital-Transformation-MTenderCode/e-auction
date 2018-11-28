package com.procurement.auction.configuration.properties

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.info
import com.procurement.auction.domain.model.date.JsonTimeDeserializer
import com.procurement.auction.domain.model.slots.Slot
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import java.time.LocalTime

data class TemplateDefaultSlot(val startTime: LocalTime,
                               val endTime: LocalTime,
                               val maxLines: Int
)

class DefaultSchedulerProperties(private val schedulerProperties: SchedulerProperties) {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    private val endTimeAllSlots: LocalTime by lazy {
        JsonTimeDeserializer.deserialize(
            schedulerProperties.endTimeAllSlots
                ?: throw IllegalStateException("Time of the end tender not set.")
        )
    }

    private val qtyLinesPerSlot: Int by lazy {
        schedulerProperties.qtyLinesPerSlot
            ?: throw IllegalStateException("Not set qty lines per slot.")
    }

    private val templatesDefaultSlots = getTemplatesDefaultSlots(schedulerProperties)
    val defaultSlots: List<Slot>
        get() = templatesDefaultSlots.map {
            Slot(
                id = SlotId(),
                startTime = it.startTime,
                endTime = it.endTime,
                maxLines = it.maxLines,
                cpids = emptySet()
            )
        }

    private fun getTemplatesDefaultSlots(schedulerProperties: SchedulerProperties): List<TemplateDefaultSlot> {
        val defaultSlots = schedulerProperties.slots!!
        if (defaultSlots.isEmpty())
            throw IllegalStateException("No slot definition.")

        val times = mutableSetOf<LocalTime>()
        val result = mutableListOf<TemplateDefaultSlot>()

        for (defaultSlot in defaultSlots) {
            val startTime = startTime(defaultSlot)
            if (!times.add(startTime))
                throw IllegalStateException("Times [$startTime] of the start slots is duplicate.")

            result.add(
                TemplateDefaultSlot(
                    startTime = startTime,
                    endTime = endTime(defaultSlot) ?: endTimeAllSlots,
                    maxLines = defaultSlot.maxLines ?: qtyLinesPerSlot
                )
            )
        }

        log.info { "Info of slots: ${slotsInfo(result)}" }
        return result
    }

    private fun startTime(slotDefinition: SchedulerProperties.DefaultSlot): LocalTime {
        val startTimeText = slotDefinition.startTime
            ?: throw IllegalStateException("Times of the start tender in slots not set.")
        return JsonTimeDeserializer.deserialize(startTimeText)
    }

    private fun endTime(slotDefinition: SchedulerProperties.DefaultSlot): LocalTime? =
        slotDefinition.endTime?.let {
            JsonTimeDeserializer.deserialize(it)
        }

    private fun slotsInfo(slots: List<TemplateDefaultSlot>): String {
        val sb = StringBuilder()
        sb.appendln("")
        for (slot in slots) {
            sb.append("time range: [")
            sb.append(slot.startTime)
            sb.append(" - ")
            sb.append(slot.endTime)
            sb.append("], max lines: ")
            sb.appendln(slot.maxLines)
        }
        return sb.toString()
    }
}