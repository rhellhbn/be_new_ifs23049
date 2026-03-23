package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.*
import org.delcom.helpers.*
import org.delcom.repositories.IBookRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class BookService(
    private val userRepo: IUserRepository,
    private val bookRepo: IBookRepository,
) {
    // GET /books
    suspend fun getAll(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val search  = call.request.queryParameters["search"] ?: ""
        val isReadP = call.request.queryParameters["is_read"]
        val isRead  = when (isReadP) { "1","true" -> true; "0","false" -> false; else -> null }
        val genre   = call.request.queryParameters["genre"]
        val page    = call.request.queryParameters["page"]?.toIntOrNull()    ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        val books = bookRepo.getAll(user.id, search, isRead, genre, page, perPage)
        val total = bookRepo.countAll(user.id, search, isRead, genre)

        call.respond(DataResponse("success", "Berhasil mengambil daftar buku",
            mapOf("books" to books, "total" to total, "page" to page, "perPage" to perPage)
        ))
    }

    // GET /books/{id}
    suspend fun getById(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val book   = bookRepo.getById(bookId)
        if (book == null || book.userId != user.id)
            throw AppException(404, "Data buku tidak tersedia!")
        call.respond(DataResponse("success", "Berhasil mengambil data buku",
            mapOf("book" to book)))
    }

    // POST /books
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val req  = call.receive<BookRequest>()
        req.userId = user.id
        val v = ValidatorHelper(req.toMap())
        v.required("title",       "Judul buku tidak boleh kosong")
        v.required("author",      "Penulis tidak boleh kosong")
        v.required("description", "Deskripsi tidak boleh kosong")
        v.validate()

        val bookId = bookRepo.create(req.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan buku",
            mapOf("bookId" to bookId)))
    }

    // PUT /books/{id}
    suspend fun put(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val req    = call.receive<BookRequest>()
        req.userId = user.id
        val v = ValidatorHelper(req.toMap())
        v.required("title",       "Judul buku tidak boleh kosong")
        v.required("author",      "Penulis tidak boleh kosong")
        v.required("description", "Deskripsi tidak boleh kosong")
        v.validate()

        val old = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id)
            throw AppException(404, "Data buku tidak tersedia!")
        req.cover = old.cover   // pertahankan cover lama

        if (!bookRepo.update(user.id, bookId, req.toEntity()))
            throw AppException(400, "Gagal memperbarui data buku!")
        call.respond(DataResponse("success", "Berhasil mengubah data buku", null))
    }

    // PUT /books/{id}/cover
    suspend fun putCover(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        var newCover: String? = null

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val filePath = "uploads/books/${UUID.randomUUID()}$ext"
                val file = File(filePath)
                file.parentFile.mkdirs()
                part.provider().copyAndClose(file.writeChannel())
                newCover = filePath
            }
            part.dispose()
        }

        if (newCover == null)          throw AppException(404, "Cover buku tidak tersedia!")
        if (!File(newCover!!).exists()) throw AppException(404, "Cover buku gagal diunggah!")

        val old = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id)
            throw AppException(404, "Data buku tidak tersedia!")

        val req = BookRequest(
            userId      = user.id,
            title       = old.title,
            author      = old.author,
            description = old.description,
            genre       = old.genre,
            isbn        = old.isbn,
            publisher   = old.publisher,
            year        = old.year,
            isRead      = old.isRead,
            cover       = newCover
        )
        if (!bookRepo.update(user.id, bookId, req.toEntity()))
            throw AppException(400, "Gagal memperbarui cover buku!")

        old.cover?.let { if (File(it).exists()) File(it).delete() }
        call.respond(DataResponse("success", "Berhasil mengubah cover buku", null))
    }

    // DELETE /books/{id}
    suspend fun delete(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val old    = bookRepo.getById(bookId)
        if (old == null || old.userId != user.id)
            throw AppException(404, "Data buku tidak tersedia!")
        if (!bookRepo.delete(user.id, bookId))
            throw AppException(400, "Gagal menghapus data buku!")
        old.cover?.let { if (File(it).exists()) File(it).delete() }
        call.respond(DataResponse("success", "Berhasil menghapus buku", null))
    }

    // GET /images/books/{id}
    suspend fun getCover(call: ApplicationCall) {
        val bookId = call.parameters["id"] ?: throw AppException(400, "ID buku tidak valid!")
        val book   = bookRepo.getById(bookId) ?: return call.respond(HttpStatusCode.NotFound)
        if (book.cover == null) throw AppException(404, "Buku belum memiliki cover")
        val file = File(book.cover!!)
        if (!file.exists()) throw AppException(404, "Cover buku tidak tersedia")
        call.respondFile(file)
    }
}
