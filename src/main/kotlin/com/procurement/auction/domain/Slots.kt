package com.procurement.auction.domain

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

interface SlotDefinition {
    val keyOfSlot: KeyOfSlot
    val startTime: LocalTime
    val endTime: LocalTime
    val maxLines: Int
    val duration: Duration
    fun hasAvailableLines(): Boolean = true
}

data class Slots(val isNew: Boolean,
                 val date: LocalDate,
                 val country: Country,
                 val definitions: TreeSet<Definition>) {

    class Definition(override val keyOfSlot: KeyOfSlot,
                     override val startTime: LocalTime,
                     override val endTime: LocalTime,
                     override val maxLines: Int,
                     val cpids: Set<CPID>) : SlotDefinition, Comparable<SlotDefinition> {

        override val duration: Duration = Duration.between(startTime, endTime)

        override fun hasAvailableLines(): Boolean = maxLines > cpids.size

        override fun compareTo(other: SlotDefinition): Int = startTime.compareTo(other.startTime)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Definition
            return keyOfSlot == other.keyOfSlot
        }

        override fun hashCode(): Int {
            return keyOfSlot
        }
    }
}
