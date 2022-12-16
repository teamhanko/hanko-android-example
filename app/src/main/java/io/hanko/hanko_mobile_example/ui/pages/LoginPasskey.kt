package io.hanko.hanko_mobile_example.ui.pages

import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
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
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val webauthnRepo: WebauthnRepo = WebAuthnRepoImpl()
    val passkeyAssertionResult = fun(activityResult: ActivityResult) {
        val dataBytes = activityResult.data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
        when {
            activityResult.resultCode != RESULT_OK -> {
                setErrorMessage("FIDO2 authentication was cancelled")
                Log.d("LoginPasskey", "FIDO2 authentication was cancelled")
            }
            dataBytes == null -> {
                setErrorMessage("Error occurred on credential assertion")
                Log.d("LoginPasskey", "Error occurred on passkey assertion")
            }
            else -> {
                Log.d(
                    "LoginPasskey",
                    "Received authentication response from Google Play Services FIDO2 API"
                )
                val credential = PublicKeyCredential.deserializeFromBytes(dataBytes)
                val response = credential.response
                if (response is AuthenticatorErrorResponse) {
                    setErrorMessage("CredentialAssertion failed: ${response.errorCode} - ${response.errorMessage}")
                    Log.d(
                        "LoginPasskey",
                        "CredentialAssertion failed: ${response.errorCode} - ${response.errorMessage}"
                    )
                } else {
                    coroutineScope.launch {
                        try {
                            val (_, token) = webauthnRepo.finalizeWebAuthnAuthentication(
                                credential
                            )
                            hankoViewModel.sessionToken = token
                            userLoggedIn()
                        } catch (ex: Exception) {
                            setErrorMessage("Failed to send assertion: got unexpected exception: ${ex.message}")
                            Log.d(
                                "LoginPasskey",
                                "Failed to send assertion: got unexpected exception: ${ex.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) {
            passkeyAssertionResult(it)
        }

    fun login() {
        coroutineScope.launch {
            try {
                val fido2ApiClient = Fido.getFido2ApiClient(context)
                val options = webauthnRepo.initWebAuthnAuthentication()

                val pIntentTask = fido2ApiClient.getSignPendingIntent(options)

                pIntentTask.addOnFailureListener {
                    setErrorMessage("authentication passkey failed: $it")
                    Log.d("LoginPasskey", "authentication passkey failed: $it")
                }

                pIntentTask.addOnSuccessListener {
                    launcher.launch(IntentSenderRequest.Builder(it).build())
                }

            } catch (ex: Exception) {
                setErrorMessage("failed to get webauthn authentication options: ${ex.message}")
                Log.d(
                    "LoginPasskey",
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