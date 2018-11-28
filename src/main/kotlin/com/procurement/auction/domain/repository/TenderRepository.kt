package com.procurement.auction.domain.repository

import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.tender.Tender

interface TenderRepository {
    fun load(cpid: CPID): Tender?

    fun saveScheduledAuctions(cpid: CPID, tender: Tender)
    fun saveCancelledAuctions(cpid: CPID, tender: Tender)
    fun saveStartedAuctions(cpid: CPID, tender: Tender)
    fun saveEndedAuctions(cpid: CPID, tender: Tender)
}