package io.hanko.hanko_mobile_example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordLogin(@SerialName("user_id") val userId: String, val password: String)

@Serializable
data class PasswordLoginResponse(val test: String?)

@Serializable
data class SetPassword(@SerialName("user_id") val userId: String, val password: String)

@Serializable
data class SetPasswordResponse(val test: String?)
