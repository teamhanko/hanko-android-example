package io.hanko.hanko_mobile_example.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import io.hanko.hanko_mobile_example.ErrorMessage
import io.hanko.hanko_mobile_example.repository.WebAuthnRepoImpl
import io.hanko.hanko_mobile_example.repository.WebauthnRepo
import io.hanko.hanko_mobile_example.view_models.HankoViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginPasskey(
    hankoViewModel: HankoViewModel,
    userLoggedIn: () -> Unit
) {
    val tag = "LoginPasskey"
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val webauthnRepo: WebauthnRepo = WebAuthnRepoImpl()

    fun login() {
        coroutineScope.launch {
            try {
                val requestJson = webauthnRepo.initWebAuthnAuthentication()

                val credentialManager = CredentialManager.create(context)

                val getCredRequest = GetCredentialRequest(
                    listOf(
                        GetPasswordOption(),
                        GetPublicKeyCredentialOption(
                            requestJson = requestJson
                        )
                    )
                )

                val credential = credentialManager.getCredential(
                    context = context,
                    request = getCredRequest,
                ).credential

                when (credential) {
                    is PublicKeyCredential -> {
                        val responseJson = credential.authenticationResponseJson

                        val (_, token) = webauthnRepo.finalizeWebAuthnAuthentication(
                            responseJson
                        )
                        hankoViewModel.sessionToken = token
                        userLoggedIn()
                    } else -> {
                        // Catch any unrecognized credential type here.
                        setErrorMessage("unknown credential")
                    }
                }
            } catch (ex: Exception) {
                setErrorMessage("failed to get webauthn authentication options: ${ex.message}")
                Log.d(
                    tag,
                    "failed to get webauthn authentication options: ${ex.message}"
                )
            }
        }
    }

    Column {
        errorMessage?.let {
            Row {
                ErrorMessage(message = it)
            }
        }
        Row {
            Button(onClick = { login() }) {
                Text(text = "Login with passkey")
            }
        }
    }
}