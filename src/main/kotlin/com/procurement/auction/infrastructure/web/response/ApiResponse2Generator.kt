package com.procurement.auction.infrastructure.web.response

import com.procurement.auction.application.service.Logger
import com.procurement.auction.configuration.properties.GlobalProperties2
import com.procurement.auction.domain.extension.nowDefaultUTC
import com.procurement.auction.domain.extension.toListOrEmpty
import com.procurement.auction.domain.fail.Fail
import com.procurement.auction.domain.fail.error.DataErrors
import com.procurement.auction.domain.fail.error.ValidationError
import com.procurement.auction.domain.model.command.id.CommandId
import com.procurement.auction.infrastructure.web.response.version.ApiVersion
import java.util.*

object ApiResponse2Generator {

    private val NaN: UUID
        get() = UUID(0, 0)

    fun generateResponseOnFailure(
        fail: Fail,
        version: ApiVersion = GlobalProperties2.App.apiVersion,
        id: CommandId = CommandId.NaN,
        logger: Logger
    ): ApiResponseV2 {
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

    private fun generateDataErrorResponse(dataError: DataErrors.Validation, version: ApiVersion, id: CommandId) =
        ApiResponseV2.Error(
            version = version,
            id = id,
            result = listOf(
                ApiResponseV2.Error.Result(
                    code = getFullErrorCode(dataError.code),
                    description = dataError.description,
                    details = ApiResponseV2.Error.Result.Detail.tryCreateOrNull(
                        name = dataError.name
                    ).toListOrEmpty()
                )
            )
        )

    private fun generateValidationErrorResponse(validationError: ValidationError, version: ApiVersion, id: CommandId) =
        ApiResponseV2.Error(
            version = version,
            id = id,
            result = listOf(
                ApiResponseV2.Error.Result(
                    code = getFullErrorCode(validationError.code),
                    description = validationError.description,
                    details = ApiResponseV2.Error.Result.Detail.tryCreateOrNull(
                        id = validationError.entityId
                    )
                        .toListOrEmpty()

                )
            )
        )

    private fun generateErrorResponse(version: ApiVersion, id: CommandId, error: Fail.Error) =
        ApiResponseV2.Error(
            version = version,
            id = id,
            result = listOf(
                ApiResponseV2.Error.Result(
                    code = getFullErrorCode(error.code),
                    description = error.description
                )
            )
        )

    private fun generateIncidentResponse(incident: Fail.Incident, version: ApiVersion, id: CommandId) =
        ApiResponseV2.Incident(
            version = version,
            id = id,
            result = ApiResponseV2.Incident.Result(
                date = nowDefaultUTC(),
                id = UUID.randomUUID().toString(),
                level = incident.level,
                service = ApiResponseV2.Incident.Result.Service(
                    id = GlobalProperties2.service.id,
                    version = GlobalProperties2.service.version,
                    name = GlobalProperties2.service.name
                ),
                details = listOf(
                    ApiResponseV2.Incident.Result.Detail(
                        code = getFullErrorCode(incident.code),
                        description = incident.description,
                        metadata = null
                    )
                )
            )
        )

    private fun getFullErrorCode(code: String): String = "${code}/${GlobalProperties2.service.id}"
}