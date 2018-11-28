package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class AmountValueException(textAmount: String, description: String? = null) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.VALIDATION_AMOUNT_VALUE,
    message = "Incorrect value of the amount: '$textAmount'." + if (description != null) " $description" else ""
)