package com.procurement.auction.domain.schedule

import com.procurement.auction.domain.Amount
import com.procurement.auction.domain.CPID
import com.procurement.auction.domain.Country
import com.procurement.auction.domain.Currency
import com.procurement.auction.domain.RelatedLot
import java.time.Duration
import java.time.LocalDate

data class AuctionsDefinition(val cpid: CPID,
                              val country: Country,
                              val tenderPeriodEnd: LocalDate,
                              val details: List<Detail>) {

    class Detail(val relatedLot: RelatedLot,
                 val duration: Duration,
                 val electronicAuctionModalities: List<ElectronicAuctionModality>) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Detail
            return relatedLot == other.relatedLot
        }

        override fun hashCode(): Int {
            return relatedLot.hashCode()
        }

        data class ElectronicAuctionModality(
            val eligibleMinimumDifference: EligibleMinimumDifference
        ) {
            data class EligibleMinimumDifference(
                val amount: Amount,
                val currency: Currency
            )
        }
    }
}