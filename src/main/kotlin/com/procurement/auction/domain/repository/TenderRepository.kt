package com.procurement.auction.domain.repository

import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.tender.TenderEntity
import com.procurement.auction.domain.model.tender.snapshot.CancelledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.EndedAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.ScheduledAuctionsSnapshot
import com.procurement.auction.domain.model.tender.snapshot.StartedAuctionsSnapshot

interface TenderRepository {
    fun loadEntity(cpid: CPID): TenderEntity?

    fun save(snapshot: ScheduledAuctionsSnapshot)
    fun save(snapshot: CancelledAuctionsSnapshot)
    fun save(snapshot: StartedAuctionsSnapshot)
    fun save(snapshot: EndedAuctionsSnapshot)
}