package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinalizeResponse(
    @SerialName("credential_id") val credentialId: String,
    @SerialName("user_id") val userId: String
)
