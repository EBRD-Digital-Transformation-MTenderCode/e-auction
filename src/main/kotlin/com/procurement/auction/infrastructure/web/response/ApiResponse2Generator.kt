package com.procurement.auction.infrastructure.web.response

import com.procurement.auction.application.service.Logger
import com.procurement.auction.configuration.properties.GlobalProperties2
import com.procurement.auction.domain.extension.nowDefaultUTC
import com.procurement.auction.domain.extension.toListOrEmpty
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.fail.error.ValidationError
import com.procurement.auction.infrastructure.web.response.version.ApiVersion2
import java.util.*

object ApiResponse2Generator {

    private val NaN: UUID
        get() = UUID(0, 0)

    fun generateResponseOnFailure(
        fail: Fail,
        version: ApiVersion2 = GlobalProperties2.App.apiVersion,
        id: UUID = NaN,
        logger: Logger
    ): ApiResponse2 {
        fail.logging(logger)
        return when (fail) {
            is Fail.Error -> {
                when (fail) {
                    is DataErrors.Validation -> generateDataErrorResponse(id = id, version = version, dataError = fail)
                    is ValidationError -> generateValidationErrorResponse(
                        id = id, version = version, validationError = fail
                    )
                    else -> generateErrorResponse(id = id, version = version, error = fail)
                }
            }
            is Fail.Incident -> generateIncidentResponse(id = id, version = version, incident = fail)
        }
    }

    private fun generateDataErrorResponse(dataError: DataErrors.Validation, version: ApiVersion2, id: UUID) =
        ApiErrorResponse2(
            version = version,
            id = id,
            result = listOf(
                ApiErrorResponse2.Error(
                    code = getFullErrorCode(dataError.code),
                    description = dataError.description,
                    details = ApiErrorResponse2.Error.Detail.tryCreateOrNull(
                        name = dataError.name
                    ).toListOrEmpty()
                )
            )
        )

    private fun generateValidationErrorResponse(validationError: ValidationError, version: ApiVersion2, id: UUID) =
        ApiErrorResponse2(
            version = version,
            id = id,
            result = listOf(
                ApiErrorResponse2.Error(
                    code = getFullErrorCode(validationError.code),
                    description = validationError.description,
                    details = ApiErrorResponse2.Error.Detail.tryCreateOrNull(
                        id = validationError.entityId
                    )
                        .toListOrEmpty()

                )
            )
        )

    private fun generateErrorResponse(version: ApiVersion2, id: UUID, error: Fail.Error) =
        ApiErrorResponse2(
            version = version,
            id = id,
            result = listOf(
                ApiErrorResponse2.Error(
                    code = getFullErrorCode(error.code),
                    description = error.description
                )
            )
        )

    private fun generateIncidentResponse(incident: Fail.Incident, version: ApiVersion2, id: UUID) =
        ApiIncidentResponse2(
            version = version,
            id = id,
            result = ApiIncidentResponse2.Incident(
                date = nowDefaultUTC(),
                id = UUID.randomUUID(),
                service = ApiIncidentResponse2.Incident.Service(
                    id = GlobalProperties2.service.id,
                    version = GlobalProperties2.service.version,
                    name = GlobalProperties2.service.name
                ),
                details = listOf(
                    ApiIncidentResponse2.Incident.Details(
                        code = getFullErrorCode(incident.code),
                        description = incident.description,
                        metadata = null
                    )
                )
            )
        )

    private fun getFullErrorCode(code: String): String = "${code}/${GlobalProperties2.service.id}"
}