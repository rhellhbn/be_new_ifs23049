package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookTable : UUIDTable("books") {
    val userId      = uuid("user_id")
    val title       = varchar("title", 255)
    val author      = varchar("author", 255)
    val description = text("description")
    val genre       = varchar("genre", 100).default("Umum")
    val isbn        = varchar("isbn", 50).nullable()
    val publisher   = varchar("publisher", 255).nullable()
    val year        = integer("year").nullable()
    val isRead      = bool("is_read").default(false)
    val cover       = text("cover").nullable()
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
