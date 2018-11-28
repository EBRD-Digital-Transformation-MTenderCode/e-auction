package com.procurement.auction.domain.model.auction

import com.procurement.auction.domain.model.auction.id.AuctionId
import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.bid.Bid
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.modality.Modality
import com.procurement.auction.domain.model.offer.Offer
import com.procurement.auction.domain.model.period.Period
import com.procurement.auction.domain.model.result.Result
import com.procurement.auction.domain.model.value.Value

class EndedAuction(
    val id: AuctionId,
    val lotId: LotId,
    val period: Period,
    val status: AuctionsStatus,
    val title: String,
    val description: String,
    val value: Value,
    val modalities: List<Modality>,
    val bids: List<Bid>,
    val progress: List<Offer>,
    val results: List<Result>
) {
    companion object {
        fun of(startedAuction: StartedAuction,
               period: Period,
               progress: List<Offer>,
               results: List<Result>
        ): EndedAuction {
            return EndedAuction(
                id = startedAuction.id,
                status = AuctionsStatus.ENDED,
                lotId = startedAuction.lotId,
                title = startedAuction.title,
                description = startedAuction.description,
                period = Period.of(period),
                value = startedAuction.value,
                modalities = startedAuction.modalities.toList(),
                bids = startedAuction.bids.toList(),
                progress = progress.toList(),
                results = results.toList()
            )
        }
    }
}




