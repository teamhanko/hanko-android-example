package io.hanko.hanko_mobile_example.ui.pages

import android.app.Activity.RESULT_OK
import android.content.Context
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
fun RegisterPasskey(
    hankoViewModel: HankoViewModel,
    onPasskeyCreated: () -> Unit,
    onSkip: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val context: Context = LocalContext.current
    val webauthnRepo: WebauthnRepo = WebAuthnRepoImpl()
    val passkeyCreationResult = fun(activityResult: ActivityResult) {
        val dataBytes = activityResult.data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
        when {
            activityResult.resultCode != RESULT_OK -> {
                setErrorMessage("FIDO2 registration was cancelled")
                Log.d("RegisterPasskey", "FIDO2 registration was cancelled")
            }
            dataBytes == null -> {
                setErrorMessage("Error occurred on credential registration")
                Log.d("RegisterPasskey", "Error occurred on credential registration")
            }
            else -> {
                Log.d(
                    "RegisterPasskey",
                    "Received register response from Google Play Services FIDO2 API"
                )
                val credential = PublicKeyCredential.deserializeFromBytes(dataBytes)
                val response = credential.response
                if (response is AuthenticatorErrorResponse) {
                    setErrorMessage("CredentialCreation failed: ${response.errorCode} - ${response.errorMessage}")
                    Log.d(
                        "RegisterPasskey",
                        "CredentialCreation failed: ${response.errorCode} - ${response.errorMessage}"
                    )
                } else {
                    coroutineScope.launch {
                        try {
                            webauthnRepo.finalizeWebAuthnRegistration(
                                hankoViewModel.sessionToken!!,
                                credential
                            )
                            onPasskeyCreated()
                        } catch (ex: Exception) {
                            setErrorMessage("failed to create credential")
                        }
                    }
                }
            }

        }
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            passkeyCreationResult(it)
        }

    fun createPasskey() {
        coroutineScope.launch {
            try {
                val fido2ApiClient = Fido.getFido2ApiClient(context)

                val options = webauthnRepo.initWebAuthnRegistration(
                    hankoViewModel.sessionToken!!,
                    hankoViewModel.userId ?: ""
                )

                val pIntentTask = fido2ApiClient.getRegisterPendingIntent(options)

                pIntentTask.addOnFailureListener {
                    Log.d("RegisterPasskey", "register passkey failed: $it")
                }
                pIntentTask.addOnSuccessListener {
                    launcher.launch(IntentSenderRequest.Builder(it).build())
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                setErrorMessage("failed to get webauthn registration options: ${ex.message}")
                Log.d(
                    "RegisterPasskey",
                    "failed to get webauthn registration options: ${ex.message}"
                )
            }
        }
    }

    Column {
        Row {
            Text(text = "Register passkey")
        }
        errorMessage?.let {
            Row {
                ErrorMessage(message = it)
            }
        }
        Row {
            Text(text = "Sign in to your account easily and securely with a passkey. Note: Your biometric data is only stored on your device and will never be shared with anyone.")
        }
        Row {
            Button(onClick = { createPasskey() }) {
                Text(text = "Save a passkey")
            }
        }
        Row {
            Button(onClick = { onSkip() }) {
                Text(text = "Skip")
            }
        }
    }
}
