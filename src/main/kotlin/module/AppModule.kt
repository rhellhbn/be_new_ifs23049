package org.delcom.module

import io.ktor.server.application.*
import org.delcom.repositories.*
import org.delcom.services.*
import org.koin.dsl.module

fun appModule(application: Application) = module {
    val baseUrl = application.environment.config
        .property("ktor.app.baseUrl").getString().trimEnd('/')
    val jwtSecret = application.environment.config
        .property("ktor.jwt.secret").getString()

    single<IUserRepository>         { UserRepository(baseUrl) }
    single<IRefreshTokenRepository> { RefreshTokenRepository() }
    single<IBookRepository>         { BookRepository(baseUrl) }

    single { UserService(get(), get()) }
    single { AuthService(jwtSecret, get(), get()) }
    single { BookService(get(), get()) }
}
