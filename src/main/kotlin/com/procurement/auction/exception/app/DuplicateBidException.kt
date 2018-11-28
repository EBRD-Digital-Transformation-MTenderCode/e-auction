package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class DuplicateBidException(bidId: BidId) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.DUPLICATE_BID,
    message = "Duplicate bid with id: '${bidId.value}'."
)