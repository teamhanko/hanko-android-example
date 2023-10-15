package io.hanko.hanko_mobile_example.repository

import io.hanko.hanko_mobile_example.client.FinishWebAuthnAuthentication
import io.hanko.hanko_mobile_example.client.FinishWebAuthnRegistration
import io.hanko.hanko_mobile_example.client.WebAuthnServiceImpl
import io.hanko.hanko_mobile_example.models.FinalizeResponse
import io.hanko.hanko_mobile_example.models.PublicKeyCreation
import io.hanko.hanko_mobile_example.models.PublicKeyRequest
import kotlinx.serialization.json.Json

interface WebauthnRepo {
    suspend fun initWebAuthnRegistration(
        token: String, userId: String
    ): String

    suspend fun finalizeWebAuthnRegistration(
        token: String, credential: String
    ): FinalizeResponse

    suspend fun initWebAuthnAuthentication(): String
    suspend fun finalizeWebAuthnAuthentication(credential: String): Pair<FinalizeResponse, String?>
}

class WebAuthnRepoImpl : WebauthnRepo {
    private val service by lazy { WebAuthnServiceImpl() }
    private val json = Json { ignoreUnknownKeys = true }
    override suspend fun initWebAuthnRegistration(
        token: String, userId: String
    ): String {
        return json.encodeToString(PublicKeyCreation.serializer(),service.initWebAuthnRegistration(token, userId).publicKey)
    }

    override suspend fun finalizeWebAuthnRegistration(
        token: String, credential: String
    ): FinalizeResponse {
        return service.finalizeWebAuthnRegistration(
            token, json.decodeFromString(FinishWebAuthnRegistration.serializer(), credential)
        )
    }

    override suspend fun initWebAuthnAuthentication(): String {
        return json.encodeToString(PublicKeyRequest.serializer(),service.initWebAuthnAuthentication().publicKey)
    }

    override suspend fun finalizeWebAuthnAuthentication(credential: String): Pair<FinalizeResponse, String?> {
        return service.finalizeWebAuthnAuthentication(json.decodeFromString(FinishWebAuthnAuthentication.serializer(), credential))
    }
}