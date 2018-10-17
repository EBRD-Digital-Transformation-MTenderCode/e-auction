package com.procurement.auction.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.*

class SlotsTest {
    companion object {
        private const val KEY_OF_SLOT_1 = 1
        private const val KEY_OF_SLOT_2 = 2
        private const val KEY_OF_SLOT_3 = 3

        private val START_TIME_SLOT_1: LocalTime = LocalTime.of(9, 0)
        private val START_TIME_SLOT_2: LocalTime = START_TIME_SLOT_1.plusHours(1)
        private val START_TIME_SLOT_3: LocalTime = START_TIME_SLOT_2.plusHours(1)

        private val END_TIME: LocalTime = LocalTime.of(16, 0)

        private const val MAX_LINES = 70
    }

    @Test
    fun comparableTest() {
        val treeSet = genTreeSet()
        val first = treeSet.first()
        assertEquals(first.keyOfSlot, KEY_OF_SLOT_1)
    }

    @Test
    fun equalsTest() {
        val one = genDetail1()
        val oneDuplicate = genDetail1()
        assertEquals(one, oneDuplicate)

        val two = genDetail2()
        assertNotEquals(one, two)
    }

    private fun genTreeSet() = TreeSet<Slots.Definition>().apply {
        add(genDetail1())
        add(genDetail2())
        add(genDetail3())
    }

    private fun genDetail1() = Slots.Definition(
        keyOfSlot = KEY_OF_SLOT_1,
        startTime = START_TIME_SLOT_1,
        endTime = END_TIME,
        maxLines = MAX_LINES,
        cpids = emptySet()
    )

    private fun genDetail2() = Slots.Definition(
        keyOfSlot = KEY_OF_SLOT_2,
        startTime = START_TIME_SLOT_2,
        endTime = END_TIME,
        maxLines = MAX_LINES,
        cpids = emptySet()
    )

    private fun genDetail3() = Slots.Definition(
        keyOfSlot = KEY_OF_SLOT_3,
        startTime = START_TIME_SLOT_3,
        endTime = END_TIME,
        maxLines = MAX_LINES,
        cpids = emptySet()
    )
}