package io.hanko.hanko_mobile_example.ui.pages

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.hanko.hanko_mobile_example.ErrorMessage
import io.hanko.hanko_mobile_example.R
import io.hanko.hanko_mobile_example.view_models.HankoViewModel
import io.ktor.client.features.*
import io.ktor.http.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmailInputForm(
    hankoViewModel: HankoViewModel,
    createUser: () -> Unit,
    loginUser: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    fun getUserInfo() {
        if (!Patterns.EMAIL_ADDRESS.matcher(hankoViewModel.email.text).matches()) {
            setErrorMessage("No valid email address")
            return
        }
        coroutineScope.launch {
            try {
                val user = hankoViewModel.getUserInfo(hankoViewModel.email.text)
                hankoViewModel.initPasscode(user.id)
                loginUser()
            } catch (ex: ClientRequestException) {
                if (ex.response.status == HttpStatusCode.NotFound) {
                    createUser()
                } else {
                    setErrorMessage(ex.message)
                }
            } catch (ex: Exception) {
                setErrorMessage(ex.message)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(1f)
    ) {
        Row {
            Text(text = "Register or Sign in")
        }
        errorMessage?.let {
            Row {
                ErrorMessage(message = it)
            }
        }
        Row {
            OutlinedTextField(
                value = hankoViewModel.email,
                onValueChange = { hankoViewModel.email = it },
                label = { Text(text = stringResource(id = R.string.email_label)) },
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .autofill(listOf(AutofillType.EmailAddress)) {
                        hankoViewModel.email = TextFieldValue(it)
                    },
                singleLine = true,
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    getUserInfo()
                }),
                isError = errorMessage != null,
                supportingText = {Text(text = errorMessage ?: "")}
            )
        }
        Row {
            Button(onClick = { getUserInfo() }) {
                Text(text = stringResource(id = R.string.continue_label))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(autofillTypes: List<AutofillType>, onFill: ((String) -> Unit)) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}
