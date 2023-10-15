package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(val password: PasswordConfig)

@Serializable
data class PasswordConfig(val enabled: Boolean? = null, @SerialName("min_password_length") val minPasswordLength: Int)
