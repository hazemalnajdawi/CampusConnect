package week11.st573015.finalproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import week11.st573015.finalproject.ui.schedule.WeeklyScheduleScreen
import week11.st573015.finalproject.ui.screens.auth.*
import week11.st573015.finalproject.ui.screens.SplashScreen
import week11.st573015.finalproject.ui.screens.home.HomeScreen
import week11.st573015.finalproject.ui.screens.tasks.TaskScreen
import week11.st573015.finalproject.ui.screens.map.MapScreen
import week11.st573015.finalproject.ui.screens.announcements.AnnouncementScreen
import week11.st573015.finalproject.vm.ScheduleViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.FORGOT) { ForgotPasswordScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }

        composable(Routes.TASKS) { TaskScreen() }
        composable(Routes.SCHEDULE) {
            WeeklyScheduleScreen(
                navToMap = { navController.navigate(Routes.MAP) },
                vm = remember { ScheduleViewModel() }
            )
        }
        composable (Routes.MAP) { MapScreen(navController) }
        composable(Routes.ANNOUNCEMENTS) { AnnouncementScreen(navController) }
    }
}
