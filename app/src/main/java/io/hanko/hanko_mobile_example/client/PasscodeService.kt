package io.hanko.hanko_mobile_example.client

import io.hanko.hanko_mobile_example.ApiRoutes
import io.hanko.hanko_mobile_example.KtorClient
import io.hanko.hanko_mobile_example.models.PasscodeFinalize
import io.hanko.hanko_mobile_example.models.PasscodeFinalizeResponse
import io.hanko.hanko_mobile_example.models.PasscodeInit
import io.hanko.hanko_mobile_example.models.PasscodeInitResponse
import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse

interface PasscodeService {
    suspend fun initPasscodeLogin(userId: String): PasscodeInitResponse
    suspend fun finalizePasscodeLogin(id: String, code: String): Pair<PasscodeFinalizeResponse, String?>
}

class PasscodeServiceImpl : PasscodeService {
    private val client by lazy { KtorClient.instance }
    override suspend fun initPasscodeLogin(userId: String): PasscodeInitResponse {
        return client.post {
            url(ApiRoutes.INIT_PASSCODE_LOGIN)
            body = PasscodeInit(userId)
        }
    }

    override suspend fun finalizePasscodeLogin(id: String, code: String): Pair<PasscodeFinalizeResponse, String?> {
        val response: HttpResponse = client.post {
            url(ApiRoutes.FINALIZE_PASSCODE_LOGIN)
            body = PasscodeFinalize(id, code)
        }

        val token = response.headers["X-Auth-Token"]

        return Pair(response.receive(), token)
    }
}