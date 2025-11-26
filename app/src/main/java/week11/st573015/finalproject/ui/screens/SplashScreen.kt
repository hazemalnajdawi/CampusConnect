package week11.st573015.finalproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import week11.st573015.finalproject.navigation.Routes

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(1500)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CampusConnect",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
