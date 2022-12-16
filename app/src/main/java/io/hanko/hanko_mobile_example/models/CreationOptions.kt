package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.Serializable

@Serializable
data class CreationOptions(
    val publicKey: PublicKeyCreation
)

@Serializable
data class PublicKeyCreation(
    val rp: RelyingParty,
    val user: User,
    val challenge: String,
    val pubKeyCredParams: Array<CredParam>,
    val timeout: Int? = null,
    val authenticatorSelection: AuthenticatorSelection? = null,
    val attestation: String? = null
)

@Serializable
data class RelyingParty(
    val id: String,
    val name: String
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val displayName: String? = null
)

@Serializable
data class CredParam(
    val type: String,
    val alg: Int
)

@Serializable
data class AuthenticatorSelection(
    val authenticatorAttachment: String? = null,
    val requireResidentKey: Boolean? = null,
    val residentKey: String? = null,
    val userVerification: String? = null
)