package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Book

@Serializable
data class BookRequest(
    var userId      : String  = "",
    var title       : String  = "",
    var author      : String  = "",
    var description : String  = "",
    var genre       : String  = "Umum",
    var isbn        : String? = null,
    var publisher   : String? = null,
    var year        : Int?    = null,
    var isRead      : Boolean = false,
    var cover       : String? = null,
) {
    fun toMap() = mapOf(
        "userId"      to userId,
        "title"       to title,
        "author"      to author,
        "description" to description,
        "genre"       to genre,
        "isbn"        to isbn,
        "publisher"   to publisher,
        "year"        to year,
        "isRead"      to isRead,
        "cover"       to cover,
    )
    fun toEntity() = Book(
        userId      = userId,
        title       = title,
        author      = author,
        description = description,
        genre       = genre.ifBlank { "Umum" },
        isbn        = isbn,
        publisher   = publisher,
        year        = year,
        isRead      = isRead,
        cover       = cover,
        updatedAt   = Clock.System.now()
    )
}
