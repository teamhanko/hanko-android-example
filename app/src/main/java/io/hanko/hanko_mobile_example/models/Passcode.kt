package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasscodeInit(@SerialName("user_id") val userId: String)

@Serializable
data class PasscodeInitResponse(
    val id: String,
    val ttl: Int,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class PasscodeFinalize(val id: String, val code: String)

@Serializable
data class PasscodeFinalizeResponse(
    val id: String,
    val ttl: Int,
    @SerialName("created_at") val createdAt: String,
    val token: String? = null
)
