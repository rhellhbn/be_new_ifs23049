package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.RefreshToken

@Serializable
data class RefreshTokenRequest(
    var userId       : String = "",
    var refreshToken : String = "",
    var authToken    : String = "",
) {
    fun toMap() = mapOf(
        "refreshToken" to refreshToken,
        "authToken"    to authToken,
    )
    fun toEntity() = RefreshToken(
        userId       = userId,
        refreshToken = refreshToken,
        authToken    = authToken,
    )
}
