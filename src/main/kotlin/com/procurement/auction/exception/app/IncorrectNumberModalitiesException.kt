package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class IncorrectNumberModalitiesException(lotId: LotId) :
    ApplicationException(
        loglevel = Logger.Level.ERROR,
        codeError = CodesOfErrors.INCORRECT_NUMBER_MODALITIES,
        message = "The lot with id: '${lotId.value}' contains the incorrect number of eligible minimum difference."
    )