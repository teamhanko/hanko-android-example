package io.hanko.hanko_mobile_example.client

import io.hanko.hanko_mobile_example.ApiRoutes
import io.hanko.hanko_mobile_example.KtorClient
import io.hanko.hanko_mobile_example.models.*
import io.ktor.client.request.*

interface UserService {
    suspend fun getUserByEmail(email: String): UserDetailsFromEmail
    suspend fun createUser(email: String): UserModel
    suspend fun getUserWithId(id: String): UserModel
    suspend fun me(token: String?): Me
}

class UserServiceImpl() : UserService {
    private val client by lazy { KtorClient.instance }
    override suspend fun getUserByEmail(email: String): UserDetailsFromEmail {
        return client.post {
            url(ApiRoutes.GET_USER_BY_EMAIL)
            body = GetUserByEmail(email)
        }
    }

    override suspend fun createUser(email: String): UserModel {
        return client.post {
            url(ApiRoutes.CREATE_USER)
            body = CreateUser(email)
        }
    }

    override suspend fun getUserWithId(id: String): UserModel {
        return client.get {
            url("${ApiRoutes.GET_USER_BY_ID}/$id")
        }
    }

    override suspend fun me(token: String?): Me {
        return client.get {
            url(ApiRoutes.GET_CURRENT_USER)
            headers {
                append("Authorization", "Bearer $token")
            }
        }
    }
}