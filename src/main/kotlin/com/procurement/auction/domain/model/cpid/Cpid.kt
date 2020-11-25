package com.procurement.auction.domain.model.cpid

import com.fasterxml.jackson.annotation.JsonValue

class Cpid private constructor(@JsonValue val underlying: String) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Cpid
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying

    companion object {
        val pattern: String
            get() = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}\$"

        private val regex = pattern.toRegex()

        fun tryCreateOrNull(value: String): Cpid? = if (value.matches(regex)) Cpid(underlying = value) else null
    }
}
