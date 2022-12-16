package io.hanko.hanko_mobile_example.client

import android.util.Log
import io.hanko.hanko_mobile_example.ApiRoutes
import io.hanko.hanko_mobile_example.KtorClient
import io.hanko.hanko_mobile_example.models.CreationOptions
import io.hanko.hanko_mobile_example.models.FinalizeResponse
import io.hanko.hanko_mobile_example.models.RequestOptions
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WebAuthnService {
    suspend fun initWebAuthnRegistration(token: String, userId: String): CreationOptions
    suspend fun finalizeWebAuthnRegistration(
        token: String,
        regResponse: FinishWebAuthnRegistration
    ): FinalizeResponse

    suspend fun initWebAuthnAuthentication(): RequestOptions
    suspend fun finalizeWebAuthnAuthentication(authResponse: FinishWebAuthnAuthentication): Pair<FinalizeResponse, String?>
}

@Serializable
data class InitWebAuthn(@SerialName("user_id") val userId: String)

@Serializable
data class FinishWebAuthnRegistration(
    val id: String,
    val rawId: String,
    val type: String,
    val response: AuthenticatorRegistrationResponse,
    val transports: Array<String>
)

@Serializable
data class FinishWebAuthnAuthentication(
    val id: String,
    val rawId: String,
    val type: String,
    val response: AuthenticatorAuthenticationResponse
)

@Serializable
data class AuthenticatorAuthenticationResponse(
    val clientDataJson: String,
    val authenticatorData: String,
    val signature: String,
    val userHandle: String?
)

@Serializable
data class AuthenticatorRegistrationResponse(
    val clientDataJSON: String,
    val attestationObject: String
)

class WebAuthnServiceImpl() : WebAuthnService {
    private val client by lazy { KtorClient.instance }
    override suspend fun initWebAuthnRegistration(token: String, userId: String): CreationOptions {
        Log.d("WebAuthnService", "webauth init with token: $token")
        return client.post {
            url(ApiRoutes.INIT_WEBAUTHN_REGISTRATION)
            headers {
                append("Authorization", "Bearer $token")
            }
            body = InitWebAuthn(userId)
        }
    }

    override suspend fun finalizeWebAuthnRegistration(
        token: String,
        regResponse: FinishWebAuthnRegistration
    ): FinalizeResponse {
        return client.post {
            url(ApiRoutes.FINALIZE_WEBAUTHN_REGISTRATION)
            headers {
                append("Authorization", "Bearer $token")
            }
            body = regResponse
        }
    }

    override suspend fun initWebAuthnAuthentication(): RequestOptions {
        return client.post {
            url(ApiRoutes.INIT_WEBAUTHN_AUTHENTICATION)
            // TODO: body
        }
    }

    override suspend fun finalizeWebAuthnAuthentication(authResponse: FinishWebAuthnAuthentication): Pair<FinalizeResponse, String?> {
        val response: HttpResponse = client.post {
            url(ApiRoutes.FINALIZE_WEBAUTHN_AUTHENTICATION)
            body = authResponse
        }

        val token = response.headers["X-Auth-Token"]

        return Pair(response.receive(), token)
    }
}