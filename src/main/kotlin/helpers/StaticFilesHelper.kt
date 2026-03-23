package org.delcom.helpers

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureStaticFiles() {
    val uploadDir = File("uploads")
    if (!uploadDir.exists()) uploadDir.mkdirs()
    routing {
        staticFiles("/static", uploadDir)
    }
}
