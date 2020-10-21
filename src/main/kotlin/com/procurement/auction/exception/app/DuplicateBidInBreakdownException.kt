package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.progressId.ProgressId
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class DuplicateBidInBreakdownException(auctionId: String, lotId: LotId, offerId: ProgressId, bidId: BidId) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.DUPLICATE_BID_IN_BREAKDOWN,
        message = "The auction with id: '$auctionId' by lot '${lotId.value}' in breakdown with id: '${offerId.value}' contains duplicate bid with id: '${bidId.value}'."
    )