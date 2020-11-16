package com.procurement.auction.domain.model.ocid

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.model.cpid.Cpid
import com.procurement.auction.domain.model.enums.EnumElementProvider.Companion.keysAsStringsUpper
import com.procurement.auction.domain.model.enums.Stage

class Ocid private constructor(@JsonValue val underlying: String, val stage: Stage) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Ocid
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying

    companion object {
        private val STAGES: String
            get() = Stage.allowedElements.keysAsStringsUpper()
                .joinToString(separator = "|", prefix = "(", postfix = ")")
        private const val STAGE_POSITION = 4

        val pattern: String
            get() = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}-$STAGES-[0-9]{13}\$"

        private val regex = pattern.toRegex()

        fun tryCreateOrNull(value: String): Ocid? = if (value.matches(regex)) {
            val stage = Stage.orNull(value.split("-")[STAGE_POSITION])!!
            Ocid(underlying = value, stage = stage)
        } else
            null
    }
}
