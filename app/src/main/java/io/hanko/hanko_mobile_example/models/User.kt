package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String,
    val email: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val verified: Boolean,
    @SerialName("webauthn_credentials") val webAuthnCredentials: Array<WebAuthnCredentials>? = null
)

@Serializable
data class WebAuthnCredentials(val id: String)

@Serializable
data class UserDetailsFromEmail(
    val id: String,
    val verified: Boolean,
    @SerialName("has_webauthn_credentials") val hasWebAuthnCredentials: Boolean? = null
)

@Serializable
data class Me(
    val id: String
)

@Serializable
data class CreateUser(val email: String)

@Serializable
data class GetUserByEmail(val email: String)
