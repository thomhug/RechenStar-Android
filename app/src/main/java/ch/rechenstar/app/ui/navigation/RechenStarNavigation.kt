package ch.rechenstar.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.rechenstar.app.domain.model.ExerciseResult
import ch.rechenstar.app.features.achievements.AchievementsScreen
import ch.rechenstar.app.features.exercise.ExerciseScreen
import ch.rechenstar.app.features.exercise.SessionCompleteScreen
import ch.rechenstar.app.features.exercise.SessionCompleteViewModel
import ch.rechenstar.app.features.home.HomeScreen
import ch.rechenstar.app.features.home.HomeUiState
import ch.rechenstar.app.features.profile.ProfileSelectionScreen
import ch.rechenstar.app.features.progress.ProgressScreen
import ch.rechenstar.app.features.settings.HelpScreen
import ch.rechenstar.app.features.settings.ParentDashboardScreen
import ch.rechenstar.app.features.settings.SettingsScreen
import ch.rechenstar.app.ui.components.ScreenTopBar

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Progress : Screen("progress")
    data object Achievements : Screen("achievements")
    data object Settings : Screen("settings")
    data object Exercise : Screen("exercise")
    data object SessionComplete : Screen("session_complete")
    data object Help : Screen("help")
    data object ParentDashboard : Screen("parent_dashboard")
    data object ProfileSelection : Screen("profile_selection")
}

private val tabRoutes = listOf(
    Screen.Home.route,
    Screen.Progress.route,
    Screen.Achievements.route,
    Screen.Settings.route
)

@Composable
fun RechenStarNavigation() {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var currentUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var currentUserName by rememberSaveable { mutableStateOf("") }

    // Exercise session state (complex objects, not saveable)
    var exerciseConfig by remember { mutableStateOf<HomeUiState?>(null) }
    var sessionResults by remember { mutableStateOf<List<ExerciseResult>>(emptyList()) }
    var sessionLength by remember { mutableIntStateOf(10) }
    var sessionStartTime by remember { mutableLongStateOf(0L) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabRoutes

    val tabTitles = listOf("Spielen", "Fortschritt", "Erfolge", "Einstellungen")

    Scaffold(
        topBar = {
            if (showBottomBar && currentUserName.isNotEmpty()) {
                ScreenTopBar(
                    title = tabTitles.getOrElse(selectedTab) { "" },
                    userName = currentUserName,
                    onProfileSwitch = {
                        currentUserId = null
                        currentUserName = ""
                        navController.navigate(Screen.ProfileSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        val route = tabRoutes[index]
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.ProfileSelection.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.ProfileSelection.route) {
                ProfileSelectionScreen(
                    onProfileSelected = { userId, userName ->
                        currentUserId = userId
                        currentUserName = userName
                        selectedTab = 0
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    userId = currentUserId,
                    onStartExercise = { config ->
                        exerciseConfig = config
                        sessionLength = config.sessionLength
                        sessionStartTime = System.currentTimeMillis()
                        navController.navigate(Screen.Exercise.route)
                    }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(userId = currentUserId)
            }

            composable(Screen.Achievements.route) {
                AchievementsScreen(userId = currentUserId)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    userId = currentUserId,
                    onShowHelp = {
                        navController.navigate(Screen.Help.route)
                    },
                    onShowParentArea = {
                        navController.navigate(Screen.ParentDashboard.route)
                    }
                )
            }

            composable(Screen.Exercise.route) {
                val config = exerciseConfig ?: return@composable
                ExerciseScreen(
                    sessionLength = config.sessionLength,
                    difficulty = config.difficulty,
                    categories = config.enabledCategories,
                    metrics = config.metrics,
                    adaptiveDifficulty = config.adaptiveDifficulty,
                    gapFillEnabled = config.gapFillEnabled,
                    hideSkipButton = config.hideSkipButton,
                    autoShowAnswerSeconds = config.autoShowAnswerSeconds,
                    onSessionComplete = { results ->
                        sessionResults = results
                        navController.navigate(Screen.SessionComplete.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onCancel = { _ ->
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.SessionComplete.route) {
                val viewModel: SessionCompleteViewModel = hiltViewModel()
                val engagement by viewModel.engagement.collectAsState()

                LaunchedEffect(sessionResults) {
                    val userId = currentUserId ?: return@LaunchedEffect
                    if (sessionResults.isNotEmpty()) {
                        viewModel.processSession(sessionResults, userId, sessionStartTime)
                    }
                }

                SessionCompleteScreen(
                    results = sessionResults,
                    sessionLength = sessionLength,
                    engagement = engagement,
                    onDismiss = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Help.route) {
                HelpScreen(onDismiss = { navController.popBackStack() })
            }

            composable(Screen.ParentDashboard.route) {
                ParentDashboardScreen(
                    userId = currentUserId,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
