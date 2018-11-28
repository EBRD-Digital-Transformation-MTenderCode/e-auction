package com.procurement.auction.domain.model.slots

import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.slots.id.SlotId
import java.time.Duration
import java.time.LocalTime

class Slot(
    val id: SlotId,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val maxLines: Int,
    cpids: Set<CPID>
) {
    private val _cpids = mutableSetOf<CPID>().apply { this.addAll(cpids) }
    val cpids: Set<CPID>
        get() = _cpids

    val duration: Duration = Duration.between(startTime, endTime)

    val isAvailable: Boolean
        get() = maxLines > cpids.size

    fun booking(cpid: CPID): Boolean = _cpids.add(cpid)

    fun release(cpid: CPID): Boolean = _cpids.remove(cpid)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Slot

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String =
        "Slot(id: '$id', start time: '$startTime', end time: '$endTime', max lines: '$maxLines', occupancy: '$cpids')"
}
