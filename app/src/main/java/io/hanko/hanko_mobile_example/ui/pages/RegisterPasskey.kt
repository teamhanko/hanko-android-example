package io.hanko.hanko_mobile_example.ui.pages

import android.content.Context
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
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
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
    val tag = "RegisterPasskey"

    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val context: Context = LocalContext.current
    val webauthnRepo: WebauthnRepo = WebAuthnRepoImpl()

    fun handleFailure(e: CreateCredentialException) {
        // TODO: implement more detailed failure management
        when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the
                // WebAuthn spec.
                e.printStackTrace()
                Log.e(tag, e.domError.toString())
            }
            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation and chose not
                // to register the credential.
                e.printStackTrace()
            }
            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                e.printStackTrace()
            }
            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing the
                // "credentials-play-services-auth" module.
                e.printStackTrace()
            }
            is CreateCredentialUnknownException -> {
                e.printStackTrace()
            }
            is CreateCredentialCustomException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
                e.printStackTrace()
            }
            else -> Log.w(tag, "Unexpected exception type ${e::class.java.name}")
        }
    }

    fun createPasskey() {
        coroutineScope.launch {
            try {

                val requestJson = webauthnRepo.initWebAuthnRegistration(
                    hankoViewModel.sessionToken!!,
                    hankoViewModel.userId ?: ""
                )

                val credentialManager = CredentialManager.create(context)

                val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
                    requestJson = requestJson,
                    preferImmediatelyAvailableCredentials = false,
                )

                val result = credentialManager.createCredential(
                    context = context,
                    request = createPublicKeyCredentialRequest,
                )

                when (result) {
                    is CreatePublicKeyCredentialResponse -> {
                        val responseJson = result.registrationResponseJson

                        try {
                            webauthnRepo.finalizeWebAuthnRegistration(
                                hankoViewModel.sessionToken!!,
                                responseJson
                            )
                            onPasskeyCreated()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            setErrorMessage("failed to create credential")
                        }
                    } else -> {
                        setErrorMessage("unknown credential")
                    }
                }
            } catch (e : CreateCredentialException){
                handleFailure(e)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setErrorMessage("failed to get webauthn registration options: ${ex.message}")
                Log.d(
                    tag,
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
