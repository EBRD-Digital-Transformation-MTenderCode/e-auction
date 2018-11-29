package com.procurement.auction.domain.repository

import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.migration.OldAuctions

interface TenderMigrationRepository {
    fun load(cpid: CPID): List<OldAuctions>
}