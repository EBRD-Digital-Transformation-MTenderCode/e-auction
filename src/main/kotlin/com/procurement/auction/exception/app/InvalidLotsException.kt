package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class InvalidLotsException(message: String) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.INVALID_LOTS,
    message = message
)