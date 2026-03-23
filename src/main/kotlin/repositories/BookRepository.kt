package org.delcom.repositories

import org.delcom.dao.BookDAO
import org.delcom.entities.Book
import org.delcom.helpers.bookDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.BookTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.util.UUID

class BookRepository(private val baseUrl: String) : IBookRepository {

    private fun buildFilter(
        userId: String,
        search: String,
        isRead: Boolean?,
        genre: String?
    ): Op<Boolean> {
        var op: Op<Boolean> = BookTable.userId eq UUID.fromString(userId)
        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            op = op and (
                    (BookTable.title.lowerCase()  like keyword) or
                            (BookTable.author.lowerCase() like keyword)
                    )
        }
        if (isRead != null)         op = op and (BookTable.isRead eq isRead)
        if (!genre.isNullOrBlank()) op = op and (BookTable.genre eq genre)
        return op
    }

    override suspend fun getAll(
        userId: String, search: String, isRead: Boolean?,
        genre: String?, page: Int, perPage: Int
    ): List<Book> = suspendTransaction {
        val offset = ((page - 1) * perPage).toLong()
        BookDAO.find { buildFilter(userId, search, isRead, genre) }
            .orderBy(
                if (search.isNotBlank()) BookTable.title to SortOrder.ASC
                else BookTable.createdAt to SortOrder.DESC
            )
            .limit(perPage).offset(offset)
            .map { bookDAOToModel(it, baseUrl) }
    }

    override suspend fun countAll(
        userId: String, search: String, isRead: Boolean?, genre: String?
    ): Long = suspendTransaction {
        BookDAO.find { buildFilter(userId, search, isRead, genre) }.count()
    }

    override suspend fun getById(bookId: String): Book? = suspendTransaction {
        BookDAO.find { BookTable.id eq UUID.fromString(bookId) }
            .limit(1).map { bookDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun create(book: Book): String = suspendTransaction {
        BookDAO.new {
            userId      = UUID.fromString(book.userId)
            title       = book.title
            author      = book.author
            description = book.description
            genre       = book.genre
            isbn        = book.isbn
            publisher   = book.publisher
            year        = book.year
            isRead      = book.isRead
            cover       = book.cover
            createdAt   = book.createdAt
            updatedAt   = book.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, bookId: String, newBook: Book): Boolean = suspendTransaction {
        val dao = BookDAO.find {
            (BookTable.id     eq UUID.fromString(bookId)) and
                    (BookTable.userId eq UUID.fromString(userId))
        }.limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.title       = newBook.title
        dao.author      = newBook.author
        dao.description = newBook.description
        dao.genre       = newBook.genre
        dao.isbn        = newBook.isbn
        dao.publisher   = newBook.publisher
        dao.year        = newBook.year
        dao.isRead      = newBook.isRead
        dao.cover       = newBook.cover
        dao.updatedAt   = newBook.updatedAt
        true
    }

    override suspend fun delete(userId: String, bookId: String): Boolean = suspendTransaction {
        BookTable.deleteWhere {
            (BookTable.id     eq UUID.fromString(bookId)) and
                    (BookTable.userId eq UUID.fromString(userId))
        } >= 1
    }
}