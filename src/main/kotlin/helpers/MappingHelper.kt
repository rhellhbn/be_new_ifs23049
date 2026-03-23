package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.BookDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Book
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun buildImageUrl(baseUrl: String, path: String): String {
    val relativePath = path.removePrefix("uploads/")
    return "$baseUrl/static/$relativePath"
}

fun userDAOToModel(dao: UserDAO, baseUrl: String) = User(
    id        = dao.id.value.toString(),
    name      = dao.name,
    username  = dao.username,
    password  = dao.password,
    photo     = dao.photo,
    urlPhoto  = if (dao.photo != null) buildImageUrl(baseUrl, dao.photo!!) else "",
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id           = dao.id.value.toString(),
    userId       = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken    = dao.authToken,
    createdAt    = dao.createdAt
)

fun bookDAOToModel(dao: BookDAO, baseUrl: String) = Book(
    id          = dao.id.value.toString(),
    userId      = dao.userId.toString(),
    title       = dao.title,
    author      = dao.author,
    description = dao.description,
    genre       = dao.genre,
    isbn        = dao.isbn,
    publisher   = dao.publisher,
    year        = dao.year,
    isRead      = dao.isRead,
    cover       = dao.cover,
    urlCover    = if (dao.cover != null) buildImageUrl(baseUrl, dao.cover!!) else "",
    createdAt   = dao.createdAt,
    updatedAt   = dao.updatedAt
)
