package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class NoBidsException : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.NO_BIDS,
    message = "No bids to perform the operation."
)