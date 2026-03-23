package org.delcom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.AuthService
import org.delcom.services.BookService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService: AuthService by inject()
    val userService: UserService by inject()
    val bookService: BookService by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(status = "error", message = cause.message ?: "Unknown error", data = "")
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Perpustakaan berjalan. Dibuat oleh IFS23049.")
        }

        // ── Auth ──────────────────────────────────────────────────────────
        route("/auth") {
            post("/register")      { authService.postRegister(call) }
            post("/login")         { authService.postLogin(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout")        { authService.postLogout(call) }
        }

        authenticate(JWTConstants.NAME) {
            // ── Users ──────────────────────────────────────────────────────
            route("/users") {
                get("/me")          { userService.getMe(call) }
                put("/me")          { userService.putMe(call) }
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo")    { userService.putMyPhoto(call) }
            }

            // ── Books ──────────────────────────────────────────────────────
            route("/books") {
                get             { bookService.getAll(call) }
                post            { bookService.post(call) }
                get("/{id}")    { bookService.getById(call) }
                put("/{id}")    { bookService.put(call) }
                put("/{id}/cover") { bookService.putCover(call) }
                delete("/{id}") { bookService.delete(call) }
            }
        }

        // ── Images publik ──────────────────────────────────────────────────
        route("/images") {
            get("users/{id}") { userService.getPhoto(call) }
            get("books/{id}") { bookService.getCover(call) }
        }
    }
}
