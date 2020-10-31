package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class UnknownBidInBreakdownException(auctionId: String, lotId: LotId, offerId: ProgressId, bidId: BidId) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.UNKNOWN_BID_IN_BREAKDOWN,
        message = "The auction with id: '$auctionId' by lot '${lotId.value}' contain unknown bid with id: '${bidId.value}' on step '${offerId.value}' in breakdown."
    )