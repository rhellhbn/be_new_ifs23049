package org.delcom.repositories

import org.delcom.dao.UserDAO
import org.delcom.entities.User
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.userDAOToModel
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class UserRepository(private val baseUrl: String) : IUserRepository {

    override suspend fun getById(userId: String): User? = suspendTransaction {
        UserDAO.find { UserTable.id eq UUID.fromString(userId) }
            .limit(1).map { userDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDAO.find { UserTable.username eq username }
            .limit(1).map { userDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun create(user: User): String = suspendTransaction {
        UserDAO.new {
            name      = user.name
            username  = user.username
            password  = user.password
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(id: String, newUser: User): Boolean = suspendTransaction {
        val dao = UserDAO.find { UserTable.id eq UUID.fromString(id) }
            .limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.name      = newUser.name
        dao.username  = newUser.username
        dao.password  = newUser.password
        dao.photo     = newUser.photo
        dao.updatedAt = newUser.updatedAt
        true
    }

    override suspend fun delete(id: String): Boolean = suspendTransaction {
        UserTable.deleteWhere { UserTable.id eq UUID.fromString(id) } >= 1
    }
}
