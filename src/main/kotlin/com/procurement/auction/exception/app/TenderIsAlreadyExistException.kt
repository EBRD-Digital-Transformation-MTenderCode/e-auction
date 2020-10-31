package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.web.dispatcher.CodesOfErrors

class TenderIsAlreadyExistException :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.TENDER_IS_ALREADY,
        message = "Tender was scheduled another client."
    )