package org.delcom.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.*
import org.delcom.entities.RefreshToken
import org.delcom.helpers.*
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepo: IUserRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    suspend fun postRegister(call: ApplicationCall) {
        val req = call.receive<AuthRequest>()
        val v   = ValidatorHelper(req.toMap())
        v.required("name",     "Nama tidak boleh kosong")
        v.required("username", "Username tidak boleh kosong")
        v.required("password", "Password tidak boleh kosong")
        v.validate()

        if (userRepo.getByUsername(req.username) != null)
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")

        req.password = hashPassword(req.password)
        val userId = userRepo.create(req.toEntity())
        call.respond(DataResponse("success", "Berhasil melakukan pendaftaran",
            mapOf("userId" to userId)))
    }

    suspend fun postLogin(call: ApplicationCall) {
        val req = call.receive<AuthRequest>()
        val v   = ValidatorHelper(req.toMap())
        v.required("username", "Username tidak boleh kosong")
        v.required("password", "Password tidak boleh kosong")
        v.validate()

        val user = userRepo.getByUsername(req.username)
            ?: throw AppException(404, "Kredensial yang digunakan tidak valid!")
        if (!verifyPassword(req.password, user.password))
            throw AppException(404, "Kredensial yang digunakan tidak valid!")

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        refreshTokenRepo.deleteByUserId(user.id)
        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepo.create(RefreshToken(
            userId       = user.id,
            authToken    = authToken,
            refreshToken = strRefreshToken
        ))

        call.respond(DataResponse("success", "Berhasil melakukan login",
            mapOf("authToken" to authToken, "refreshToken" to strRefreshToken)))
    }

    suspend fun postRefreshToken(call: ApplicationCall) {
        val req = call.receive<RefreshTokenRequest>()
        val v   = ValidatorHelper(req.toMap())
        v.required("refreshToken", "Refresh Token tidak boleh kosong")
        v.required("authToken",    "Auth Token tidak boleh kosong")
        v.validate()

        val existing = refreshTokenRepo.getByToken(req.refreshToken, req.authToken)
        refreshTokenRepo.delete(req.authToken)
        if (existing == null) throw AppException(401, "Token tidak valid!")

        val user = userRepo.getById(existing.userId)
            ?: throw AppException(404, "User tidak valid!")

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepo.create(RefreshToken(
            userId       = user.id,
            authToken    = authToken,
            refreshToken = strRefreshToken
        ))

        call.respond(DataResponse("success", "Berhasil melakukan refresh token",
            mapOf("authToken" to authToken, "refreshToken" to strRefreshToken)))
    }

    suspend fun postLogout(call: ApplicationCall) {
        val req = call.receive<RefreshTokenRequest>()
        val v   = ValidatorHelper(req.toMap())
        v.required("authToken", "Auth Token tidak boleh kosong")
        v.validate()

        val decoded = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(req.authToken)
        val userId  = decoded.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")

        refreshTokenRepo.delete(req.authToken)
        refreshTokenRepo.deleteByUserId(userId)
        call.respond(DataResponse("success", "Berhasil logout", null))
    }
}
