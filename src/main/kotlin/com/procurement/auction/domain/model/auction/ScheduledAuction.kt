package com.procurement.auction.domain.model.auction

import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.modality.Modality
import java.time.LocalDateTime

open class ScheduledAuction(
    val id: AuctionId,
    val lotId: LotId,
    val startDate: LocalDateTime,
    val status: AuctionsStatus,
    val modalities: List<Modality>
) {
    companion object {
        fun of(id: AuctionId,
               lotId: LotId,
               startDate: LocalDateTime,
               modalities: List<Modality>): ScheduledAuction {
            return ScheduledAuction(
                id = id,
                status = AuctionsStatus.SCHEDULED,
                lotId = lotId,
                startDate = startDate,
                modalities = modalities
            )
        }
    }
}




