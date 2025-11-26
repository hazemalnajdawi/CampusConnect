package week11.st573015.finalproject.ui.screens.auth

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
import week11.st573015.finalproject.navigation.Routes
import week11.st573015.finalproject.ui.components.AuthTextField
import week11.st573015.finalproject.vm.AuthState
import week11.st573015.finalproject.vm.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        if (name.isBlank()) { localError = "Name required"; return false }
        if (email.isBlank()) { localError = "Email required"; return false }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { localError = "Enter valid email"; return false }
        if (password.length < 6) { localError = "Password must be at least 6 characters"; return false }
        if (password != passwordConfirm) { localError = "Passwords do not match"; return false }
        localError = null
        return true
    }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Success -> {
                if ((state as AuthState.Success).user != null) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(ctx, (state as AuthState.Error).message, Toast.LENGTH_LONG).show()
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

            Text("Create an account", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(value = name, onValueChange = { name = it }, label = "Full name")
            Spacer(modifier = Modifier.height(8.dp))

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
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                label = { Text("Confirm password") },
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
                if (validate()) vm.register(email.trim(), password, name.trim())
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Already have an account? Sign in")
            }
        }
    }
}
