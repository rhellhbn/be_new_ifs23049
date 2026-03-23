package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id        : String  = "",
    val name      : String  = "",
    val username  : String  = "",
    val photo     : String? = null,
    val createdAt : String  = "",
    val updatedAt : String  = "",
)
