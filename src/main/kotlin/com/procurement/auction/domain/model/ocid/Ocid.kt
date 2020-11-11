package com.procurement.auction.domain.model.ocid

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.auction.domain.model.enums.EnumElementProvider.Companion.keysAsStringsUpper
import com.procurement.auction.domain.model.enums.Stage

class Ocid private constructor(private val value: String, val stage: Stage) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Ocid
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value

    companion object {
        private val STAGES: String
            get() = Stage.allowedElements.keysAsStringsUpper()
                .joinToString(separator = "|", prefix = "(", postfix = ")")
        private const val STAGE_POSITION = 4

        private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}-$STAGES-[0-9]{13}\$".toRegex()

        val pattern: String
            get() = regex.pattern

        fun tryCreateOrNull(value: String): Ocid? = if (value.matches(regex)) {
            val stage = Stage.orNull(value.split("-")[STAGE_POSITION])!!
            Ocid(value = value, stage = stage)
        } else null
    }
}
