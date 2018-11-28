package com.procurement.auction.domain.factory

import com.procurement.auction.domain.model.auction.status.AuctionsStatus
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.cpid.CPID
import com.procurement.auction.domain.model.operationId.OperationId
import com.procurement.auction.domain.model.tender.Tender
import com.procurement.auction.domain.model.tender.snapshot.TenderSnapshot
import com.procurement.auction.domain.model.version.RowVersion

interface TenderFactory {
    fun create(rowVersion: RowVersion,
               operationId: OperationId,
               id: CPID,
               country: Country,
               auctionsStatus: AuctionsStatus,
               data: TenderSnapshot.Data): Tender
}
