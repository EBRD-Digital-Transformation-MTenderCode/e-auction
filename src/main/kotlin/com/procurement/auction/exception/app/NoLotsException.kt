package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class NoLotsException : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.NO_LOTS,
    message = "No lots to perform the operation."
)