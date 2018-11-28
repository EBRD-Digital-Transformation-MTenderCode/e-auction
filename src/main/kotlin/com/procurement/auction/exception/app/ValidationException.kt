package com.procurement.auction.exception.app

import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.infrastructure.dispatcher.CodesOfErrors

class ValidationException(attributeName: String, attributeValue: String) : ApplicationException(
    loglevel = Logger.Level.ERROR,
    codeError = CodesOfErrors.VALIDATION_ATTRIBUTE,
    message = "The attribute '$attributeName' has an incorrect value: '$attributeValue'."
)