package com.procurement.auction.domain.model.auction

import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.Bid
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.modality.Modality
import com.procurement.auction.domain.model.value.Value
import java.time.LocalDateTime

class StartedAuction(
    val id: AuctionId,
    val lotId: LotId,
    val startDate: LocalDateTime,
    val status: AuctionsStatus,
    val title: String,
    val description: String,
    val value: Value,
    val modalities: List<Modality>,
    val bids: List<Bid>
) {
    companion object {
        fun of(scheduledAuction: ScheduledAuction,
               title: String,
               description: String,
               value: Value,
               bids: List<Bid>
        ): StartedAuction {
            for (bid in bids) {
                if (bid.relatedLot != scheduledAuction.lotId)
                    throw IllegalArgumentException("The bid for an unknown lot.")
            }

            return StartedAuction(
                id = scheduledAuction.id,
                status = AuctionsStatus.STARTED,
                lotId = scheduledAuction.lotId,
                title = title,
                description = description,
                startDate = scheduledAuction.startDate,
                value = value,
                modalities = scheduledAuction.modalities.toList(),
                bids = bids
            )
        }
    }
}




