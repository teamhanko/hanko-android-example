package io.hanko.hanko_mobile_example

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

object ApiRoutes {
    private const val BASE_URL = "<YOUR_HANKO_API>"

    const val GET_CONFIG = "$BASE_URL/.well-known/config"

    const val INIT_WEBAUTHN_REGISTRATION = "$BASE_URL/webauthn/registration/initialize"
    const val FINALIZE_WEBAUTHN_REGISTRATION = "$BASE_URL/webauthn/registration/finalize"

    const val INIT_WEBAUTHN_AUTHENTICATION = "$BASE_URL/webauthn/login/initialize"
    const val FINALIZE_WEBAUTHN_AUTHENTICATION = "$BASE_URL/webauthn/login/finalize"

    const val GET_USER_BY_EMAIL = "$BASE_URL/user"
    const val GET_CURRENT_USER = "$BASE_URL/me"
    const val CREATE_USER = "$BASE_URL/users"
    const val GET_USER_BY_ID = "$BASE_URL/users"

    const val INIT_PASSCODE_LOGIN = "$BASE_URL/passcode/login/initialize"
    const val FINALIZE_PASSCODE_LOGIN = "$BASE_URL/passcode/login/finalize"

    const val PASSWORD_LOGIN = "$BASE_URL/password/login"
    const val SET_PASSWORD = "$BASE_URL/password"
}

object KtorClient {
    private val client = HttpClient(Android) {
        expectSuccess = true
        install(Logging) {
            logger = CustomHttpLogger
            level = LogLevel.ALL
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            if (method != HttpMethod.Get) contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    val instance = client
}

private object CustomHttpLogger : Logger {
    private const val tag = "HttpLogger"

    override fun log(message: String) {
        Log.i(tag, message)
    }
}