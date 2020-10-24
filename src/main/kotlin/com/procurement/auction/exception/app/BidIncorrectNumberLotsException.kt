package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class BidIncorrectNumberLotsException(bidId: BidId) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.BID_INCORRECT_NUMBER_LOTS,
    message = "The bid with id: '${bidId.value}' contains incorrect number of lots."
)