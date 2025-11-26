package week11.st573015.finalproject.ui.screens.auth

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import week11.st573015.finalproject.navigation.Routes
import week11.st573015.finalproject.ui.components.AuthTextField
import week11.st573015.finalproject.vm.AuthState
import week11.st573015.finalproject.vm.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    RequestNotificationPermission()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        if (email.isBlank()) { localError = "Email required"; return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { localError = "Enter valid email"; return false }
        if (password.length < 6) { localError = "Password must be at least 6 characters"; return false }
        localError = null
        return true
    }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success -> {
                if ((state as AuthState.Success).user != null) {
                    // navigate to home
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (state as AuthState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Text("CampusConnect", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when (state) {
                is AuthState.Loading -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
                is AuthState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (validate()) vm.login(email.trim(), password)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
                Text("Create account")
            }

            TextButton(onClick = { navController.navigate(Routes.FORGOT) }) {
                Text("Forgot password?")
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) Log.d("Permission", "Notification permission granted")
            else Log.d("Permission", "Notification permission denied")
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
