package org.delcom.repositories

import org.delcom.entities.Book

interface IBookRepository {
    suspend fun getAll(userId: String, search: String, isRead: Boolean?, genre: String?, page: Int, perPage: Int): List<Book>
    suspend fun countAll(userId: String, search: String, isRead: Boolean?, genre: String?): Long
    suspend fun getById(bookId: String): Book?
    suspend fun create(book: Book): String
    suspend fun update(userId: String, bookId: String, newBook: Book): Boolean
    suspend fun delete(userId: String, bookId: String): Boolean
}
