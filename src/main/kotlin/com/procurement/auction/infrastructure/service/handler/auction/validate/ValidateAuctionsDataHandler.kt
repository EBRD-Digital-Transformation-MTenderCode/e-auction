package com.procurement.auction.infrastructure.service.handler.auction.validate

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.auction.application.params.auction.validate.convert
import com.procurement.auction.application.service.Logger
import com.procurement.auction.application.service.Transform
import com.procurement.auction.application.service.auctions.ValidateAuctionsService
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.functional.ValidationResult
import com.procurement.auction.domain.functional.asValidationFailure
import com.procurement.auction.domain.functional.bind
import com.procurement.auction.infrastructure.service.command.type.Command2Type
import com.procurement.auction.infrastructure.service.handler.AbstractValidationHandler2
import com.procurement.auction.infrastructure.web.request.tryGetParams

import org.springframework.stereotype.Component

@Component
class ValidateAuctionsDataHandler(
    logger: Logger,
    private val transform: Transform,
    private val validateAuctionsService: ValidateAuctionsService
) : AbstractValidationHandler2<Command2Type, Fail>(
    logger = logger
) {
    override val action: Command2Type = Command2Type.VALIDATE_AUCTIONS_DATA

    override fun execute(node: JsonNode): ValidationResult<Fail> {
        val params = node.tryGetParams(ValidateAuctionsDataRequest::class.java, transform = transform)
            .bind { it.convert() }
            .doReturn { error -> return error.asValidationFailure() }

        return validateAuctionsService.validateAuctionsData(params)
    }
}