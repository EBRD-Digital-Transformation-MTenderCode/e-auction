package com.procurement.auction.domain

import java.time.Duration
import java.time.LocalDate

data class LotsInfo(val cpid: String,
                    val country: String,
                    val tenderPeriodEnd: LocalDate,
                    val details: List<Detail>) {

    class Detail(val relatedLot: RelatedLot,
                 val duration: Duration,
                 val amount: Double,
                 val currency: String) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Detail
            return relatedLot == other.relatedLot
        }

        override fun hashCode(): Int {
            return relatedLot.hashCode()
        }

        companion object {
            val comparator = Comparator<Detail> { left, right ->
                if (left == null) return@Comparator 1
                if (right == null) return@Comparator -1
                if (left.relatedLot == right.relatedLot) return@Comparator 0
                left.relatedLot.compareTo(right.relatedLot)
            }
        }
    }
}