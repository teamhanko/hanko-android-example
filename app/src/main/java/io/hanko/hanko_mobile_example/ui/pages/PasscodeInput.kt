package io.hanko.hanko_mobile_example.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.hanko.hanko_mobile_example.ErrorMessage
import io.hanko.hanko_mobile_example.R
import io.hanko.hanko_mobile_example.view_models.HankoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeInput(
    hankoViewModel: HankoViewModel,
    userLoggedIn: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    var code by remember { mutableStateOf(TextFieldValue()) }

    fun login() {
        coroutineScope.launch {
            try {
                val (_, token) = hankoViewModel.finalizePasscode(code.text)
                hankoViewModel.sessionToken = token
                userLoggedIn()
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
        errorMessage?.let {
            Row {
                ErrorMessage(message = it)
            }
        }
        Row {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text(text = "Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row {
            Button(onClick = { login() }) {
                Text(text = stringResource(id = R.string.continue_label))
            }
        }
    }
}
