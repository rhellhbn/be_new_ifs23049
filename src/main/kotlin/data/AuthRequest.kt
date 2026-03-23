package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.User

@Serializable
data class AuthRequest(
    var name        : String = "",
    var username    : String = "",
    var password    : String = "",
    var newPassword : String = "",
) {
    fun toMap() = mapOf(
        "name"        to name,
        "username"    to username,
        "password"    to password,
        "newPassword" to newPassword,
    )
    fun toEntity() = User(
        name      = name,
        username  = username,
        password  = password,
        updatedAt = Clock.System.now()
    )
}
