package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestOptions(
    val publicKey: PublicKeyRequest
)

@Serializable
data class PublicKeyRequest(
    val challenge: String,
    val timeout: Int? = null,
    val rpId: String,
    val userVerification: String? = null,
    val allowCredentials: Array<AllowCredential>? = null
)

@Serializable
data class AllowCredential(val type: String, val id: String)