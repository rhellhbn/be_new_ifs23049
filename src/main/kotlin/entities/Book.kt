package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Book(
    var id          : String  = UUID.randomUUID().toString(),
    var userId      : String,
    var title       : String,
    var author      : String,
    var description : String,
    var genre       : String  = "Umum",
    var isbn        : String? = null,
    var publisher   : String? = null,
    var year        : Int?    = null,
    var isRead      : Boolean = false,
    var cover       : String? = null,
    var urlCover    : String  = "",
    @Contextual val createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)
