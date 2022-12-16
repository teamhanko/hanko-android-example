package io.hanko.hanko_mobile_example.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.hanko.hanko_mobile_example.ErrorMessage
import io.hanko.hanko_mobile_example.view_models.HankoViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateUser(
    hankoViewModel: HankoViewModel,
    userCreated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    fun createUser() {
        coroutineScope.launch {
            try {
                val user = hankoViewModel.createUser(hankoViewModel.email.text)
                hankoViewModel.initPasscode(user.id)
                userCreated()
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
            Text(text = "Do you want to register ${hankoViewModel.email.text}")

        }
        Row {
            Button(onClick = ::createUser) {
                Text(text = "Create User")
            }

        }
    }
}