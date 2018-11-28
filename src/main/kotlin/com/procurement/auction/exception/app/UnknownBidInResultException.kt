package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class UnknownBidInResultException(auctionId: String, lotId: LotId, bidId: BidId) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.UNKNOWN_BID_IN_RESULT,
    message = "The auction with id: '$auctionId' by lot '${lotId.value}' contain unknown bid with id: '${bidId.value}' in results."
)