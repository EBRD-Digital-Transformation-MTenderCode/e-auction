package com.procurement.auction.domain.model.version

import com.procurement.auction.domain.model.ValueObject

data class ApiVersion(val major: Int, val minor: Int, val patch: Int) : ValueObject {
    companion object {
        fun valueOf(version: String): ApiVersion {
            val split = version.split(".")
            if (split.isEmpty() || split.size > 3)
                throw IllegalArgumentException("Invalid value of the api rowVersion ($version).")

            val major: Int = split[0].toIntOrNull()
                ?: throw  IllegalArgumentException("Invalid value of the api rowVersion ($version).")

            val minor: Int =
                if (split.size >= 2) {
                    split[1].toIntOrNull()
                        ?: throw  IllegalArgumentException("Invalid value of the api rowVersion ($version).")
                } else 0

            val patch: Int =
                if (split.size == 3) {
                    split[2].toIntOrNull()
                        ?: throw  IllegalArgumentException("Invalid value of the api rowVersion ($version).")
                } else 0

            return ApiVersion(major = major, minor = minor, patch = patch)
        }
    }
}