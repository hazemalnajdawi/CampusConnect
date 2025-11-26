package week11.st573015.finalproject.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast
import week11.st573015.finalproject.ui.components.AuthTextField
import week11.st573015.finalproject.vm.AuthViewModel

@Composable
fun ForgotPasswordScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Text("Reset password", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ctx, "Enter a valid email", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                loading = true
                vm.sendPasswordReset(email.trim()) { result ->
                    loading = false
                    if (result.isSuccess) {
                        Toast.makeText(ctx, "Password reset email sent", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(ctx, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Send reset email")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.popBackStack() }) { Text("Back to login") }
        }
    }
}