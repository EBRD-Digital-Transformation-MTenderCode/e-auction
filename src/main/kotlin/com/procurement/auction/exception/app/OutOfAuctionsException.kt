package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class OutOfAuctionsException : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.OUT_OF_LOTS,
    message = "Too many lots to complete the operation."
)