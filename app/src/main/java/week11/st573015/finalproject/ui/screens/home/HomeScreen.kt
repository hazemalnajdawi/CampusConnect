package week11.st573015.finalproject.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.*
import week11.st573015.finalproject.navigation.Routes
import week11.st573015.finalproject.ui.screens.map.MapScreen
import week11.st573015.finalproject.ui.screens.tasks.TaskScreen
import week11.st573015.finalproject.ui.schedule.WeeklyScheduleScreen
import week11.st573015.finalproject.vm.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class BottomNavItem(val route: String, val label: String, val icon: @Composable () -> Unit) {
    data object Tasks : BottomNavItem(Routes.TASKS, "Tasks", { Icon(Icons.Default.List, contentDescription = "Tasks") })
    data object Schedule : BottomNavItem(Routes.SCHEDULE, "Schedule", { Icon(Icons.Default.DateRange, contentDescription = "Schedule") })
    data object Map : BottomNavItem(Routes.MAP, "Map", { Icon(Icons.Default.Place, contentDescription = "Map") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Tasks,
        BottomNavItem.Schedule,
        BottomNavItem.Map
    )

    val currentBackStack by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Routes.TASKS

    val screenTitle = when (currentRoute) {
        Routes.TASKS -> "Tasks"
        Routes.SCHEDULE -> "Weekly Schedule"
        Routes.MAP -> "Classroom Map"
        else -> ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.ANNOUNCEMENTS) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Announcements")
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = item.icon
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.TASKS) { TaskScreen() }

            composable(Routes.SCHEDULE) {
                WeeklyScheduleScreen(
                    navToMap = { bottomNavController.navigate(Routes.MAP) },
                    vm = remember { ScheduleViewModel() }
                )
            }

            composable(Routes.MAP) {
                MapScreen(navController)
            }
        }
    }
}
