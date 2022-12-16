package io.hanko.hanko_mobile_example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import io.hanko.hanko_mobile_example.ui.pages.EmailInputForm
import io.hanko.hanko_mobile_example.ui.pages.LoginPasskey
import io.hanko.hanko_mobile_example.ui.pages.PasscodeInput
import io.hanko.hanko_mobile_example.ui.pages.RegisterPasskey
import io.hanko.hanko_mobile_example.ui.theme.Theme
import io.hanko.hanko_mobile_example.view_models.HankoViewModel
import io.ktor.client.features.*
import kotlinx.serialization.SerializationException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sessionToken: String? = null
        setContent {
            Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            HankoComponent(loggedIn = { token ->
                                sessionToken = token
                                navController.navigate("secure_content")
                            })
                        }
                        composable("secure_content") {
                            SecureContent(
                                sessionToken,
                                onLogout = { navController.navigate("login") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HankoComponent(
    loggedIn: (String) -> Unit,
    hankoViewModel: HankoViewModel = viewModel(),
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "hanko_component") {
        loginGraph(
            navController,
            loggedIn,
            hankoViewModel
        )
    }
}

fun NavGraphBuilder.loginGraph(
    navController: NavController,
    loggedIn: (String) -> Unit,
    hankoViewModel: HankoViewModel,
) {
    navigation(startDestination = "hanko/init", route = "hanko_component") {
        composable("hanko/init") {
            InitLoader(
                hankoViewModel = hankoViewModel,
                configLoaded = {
                    navController.navigate("hanko/email_input")
                    {
                        popUpTo("hanko/init") {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("hanko/email_input") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row {
                    EmailInputForm(
                        hankoViewModel = hankoViewModel,
                        createUser = { navController.navigate("hanko/create_user") },
                        loginUser = {
                            navController.navigate("hanko/passcode_input")
                        }
                    )
                }
                Row {
                    LoginPasskey(
                        hankoViewModel = hankoViewModel,
                        userLoggedIn = { loggedIn(hankoViewModel.sessionToken ?: "") })
                }
            }
        }
        composable("hanko/create_user") {
            io.hanko.hanko_mobile_example.ui.pages.CreateUser(
                hankoViewModel = hankoViewModel,
                userCreated = { navController.navigate("hanko/passcode_input") }
            )
        }
        composable("hanko/passcode_input") {
            PasscodeInput(
                hankoViewModel = hankoViewModel,
                userLoggedIn = {
                    navController.navigate("hanko/register_passkey")
                }
            )
        }
        composable("hanko/register_passkey") {
            RegisterPasskey(
                hankoViewModel = hankoViewModel,
                onPasskeyCreated = { loggedIn(hankoViewModel.sessionToken ?: "") },
                onSkip = { loggedIn(hankoViewModel.sessionToken ?: "") })
        }
    }
}

@Composable
fun SecureContent(token: String?, onLogout: () -> Unit) {
    Column {
        Row { Text(text = "This is the secure content you want to read.") }
        Row { Text(text = "SessionToken: $token") }
        Row {
            Button(onClick = { onLogout() }) {
                Text(text = "Logout")
            }
        }
    }
}

@Composable
fun InitLoader(
    hankoViewModel: HankoViewModel,
    configLoaded: () -> Unit
) {
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            hankoViewModel.loadConfig()
            Log.d(MainActivity::class.simpleName, "config loaded")
            configLoaded()
            Log.d(MainActivity::class.simpleName, "redirected to email input")
        } catch (ex: ResponseException) {
            Log.d(
                MainActivity::class.simpleName,
                "failed to load config: got ResponseException: " + ex.message
            )
            setErrorMessage(ex.message)
        } catch (ex: SerializationException) {
            Log.d(
                MainActivity::class.simpleName,
                "failed to load config: got SerializationException: " + ex.message
            )
            setErrorMessage(ex.message)
        } catch (ex: Exception) {
            Log.d(
                MainActivity::class.simpleName,
                "failed to load config: got an unexpected exception: " + ex.message
            )
            setErrorMessage(ex.message)
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        if (errorMessage != null) {
            ErrorMessage(message = errorMessage)
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Column {
        Text(
            text = message, modifier = Modifier.border(
                1.dp, MaterialTheme.colorScheme.error, MaterialTheme.shapes.medium
            )
        )
    }
}
