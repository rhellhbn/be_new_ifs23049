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
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class UserService(
    private val userRepo: IUserRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        call.respond(DataResponse("success", "Berhasil mengambil informasi akun saya",
            mapOf("user" to UserResponse(
                id        = user.id,
                name      = user.name,
                username  = user.username,
                photo     = user.urlPhoto.ifEmpty { null },
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt.toString(),
            ))
        ))
    }

    suspend fun putMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val req  = call.receive<AuthRequest>()
        val v    = ValidatorHelper(req.toMap())
        v.required("name",     "Nama tidak boleh kosong")
        v.required("username", "Username tidak boleh kosong")
        v.validate()

        val existing = userRepo.getByUsername(req.username)
        if (existing != null && existing.id != user.id)
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")

        user.name     = req.name
        user.username = req.username
        if (!userRepo.update(user.id, user))
            throw AppException(400, "Gagal memperbarui data profile!")
        call.respond(DataResponse("success", "Berhasil mengubah data profile", null))
    }

    suspend fun putMyPassword(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val req  = call.receive<AuthRequest>()
        val v    = ValidatorHelper(req.toMap())
        v.required("password",    "Kata sandi lama tidak boleh kosong")
        v.required("newPassword", "Kata sandi baru tidak boleh kosong")
        v.validate()

        if (!verifyPassword(req.password, user.password))
            throw AppException(404, "Kata sandi lama tidak valid!")

        user.password = hashPassword(req.newPassword)
        if (!userRepo.update(user.id, user))
            throw AppException(400, "Gagal mengubah kata sandi!")

        refreshTokenRepo.deleteByUserId(user.id)
        call.respond(DataResponse("success", "Berhasil mengubah kata sandi", null))
    }

    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        var newPhoto: String? = null

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val filePath = "uploads/users/${UUID.randomUUID()}$ext"
                val file = File(filePath)
                file.parentFile.mkdirs()
                part.provider().copyAndClose(file.writeChannel())
                newPhoto = filePath
            }
            part.dispose()
        }

        if (newPhoto == null)         throw AppException(404, "Photo profile tidak tersedia!")
        if (!File(newPhoto!!).exists()) throw AppException(404, "Photo profile gagal diunggah!")

        val oldPhoto = user.photo
        user.photo = newPhoto
        if (!userRepo.update(user.id, user))
            throw AppException(400, "Gagal memperbarui photo profile!")

        oldPhoto?.let { if (File(it).exists()) File(it).delete() }
        call.respond(DataResponse("success", "Berhasil mengubah photo profile", null))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid!")
        val user   = userRepo.getById(userId) ?: throw AppException(404, "User tidak ditemukan!")
        if (user.photo == null) throw AppException(404, "User belum memiliki photo profile")
        val file = File(user.photo!!)
        if (!file.exists()) throw AppException(404, "Photo profile tidak tersedia")
        call.respondFile(file)
    }
}
