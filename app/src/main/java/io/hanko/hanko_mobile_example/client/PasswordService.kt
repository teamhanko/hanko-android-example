package io.hanko.hanko_mobile_example.client

import io.hanko.hanko_mobile_example.ApiRoutes
import io.hanko.hanko_mobile_example.KtorClient
import io.hanko.hanko_mobile_example.models.PasswordLogin
import io.hanko.hanko_mobile_example.models.PasswordLoginResponse
import io.hanko.hanko_mobile_example.models.SetPassword
import io.hanko.hanko_mobile_example.models.SetPasswordResponse
import io.ktor.client.request.*

interface PasswordService {
    suspend fun loginPassword(userId: String, password: String): PasswordLoginResponse
    suspend fun setPassword(userId: String, password: String): SetPasswordResponse
}

class PasswordServiceImpl() : PasswordService {
    private val client by lazy { KtorClient.instance }
    override suspend fun loginPassword(userId: String, password: String): PasswordLoginResponse {
        return client.post {
            url(ApiRoutes.PASSWORD_LOGIN)
            body = PasswordLogin(userId, password)
        }
    }

    override suspend fun setPassword(userId: String, password: String): SetPasswordResponse {
        return client.put {
            url(ApiRoutes.SET_PASSWORD)
            body = SetPassword(userId, password)
        }
    }
}