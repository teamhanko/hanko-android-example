package io.hanko.hanko_mobile_example.view_models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import io.hanko.hanko_mobile_example.client.*
import io.hanko.hanko_mobile_example.models.*

class HankoViewModel() : ViewModel() {
    private val userService: UserService by lazy { UserServiceImpl() }
    private val configService: ConfigService by lazy { ConfigServiceImpl() }
    private val passcodeService: PasscodeService by lazy { PasscodeServiceImpl() }
    private val passwordService: PasswordService by lazy { PasswordServiceImpl() }

    var sessionToken: String? = null
    var config: Config? = null
    var userId: String? = null
    var email by mutableStateOf(TextFieldValue()) // remove here and move to the composable, maybe use rememberSaveable
    var passcode: PasscodeInitResponse? = null

    suspend fun loadConfig(): Config {
        config = configService.getConfig()
        return config!!
    }

    suspend fun getUserInfo(email: String): UserDetailsFromEmail {
        val userInfo = userService.getUserByEmail(email)
        userId = userInfo.id
        return userInfo
    }

    suspend fun createUser(email: String): UserModel {
        val user = userService.createUser(email)
        userId = user.id
        return user
    }

    suspend fun initPasscode(userId: String): PasscodeInitResponse {
        passcode = passcodeService.initPasscodeLogin(userId)
        return passcode!!
    }

    suspend fun finalizePasscode(code: String): Pair<PasscodeFinalizeResponse, String?> {
        return passcodeService.finalizePasscodeLogin(passcode!!.id, code)
    }

    suspend fun loginWithPassword(userId: String, password: String): PasswordLoginResponse {
        return passwordService.loginPassword(userId, password)
    }

    suspend fun setPassword(userId: String, newPassword: String): SetPasswordResponse {
        return passwordService.setPassword(userId, newPassword)
    }
}