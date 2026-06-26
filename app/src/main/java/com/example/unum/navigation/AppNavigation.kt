package com.example.unum.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unum.data.model.FortuneBook
import com.example.unum.presentation.ActionPlanScreen
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.FlowReportScreen
import com.example.unum.presentation.HomeScreen
import com.example.unum.presentation.InputScreen
import com.example.unum.presentation.LibraryScreen
import com.example.unum.presentation.NotificationOnboardingScreen
import com.example.unum.presentation.PaymentScreen
import com.example.unum.presentation.PremiumScreen
import com.example.unum.presentation.ReaderScreen
import com.example.unum.presentation.ResultScreen
import com.example.unum.presentation.SettingsScreen
import com.example.unum.ui.components.BottomNavBar

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Notification : AppRoute("notification")
    data object Input : AppRoute("input")
    data object Fortune : AppRoute("fortune")
    data object FlowReport : AppRoute("flow")
    data object ActionPlan : AppRoute("action")
    data object Premium : AppRoute("premium")
    data object Payment : AppRoute("payment")
    data object Library : AppRoute("library")
    data object Settings : AppRoute("settings")
    data object Reader : AppRoute("reader/{bookId}") {
        fun create(bookId: String) = "reader/$bookId"
    }
}

@Composable
fun UnumAppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoute.Home.route

    val bottomNavRoute = when {
        currentRoute.startsWith("reader/") -> AppRoute.Library.route
        currentRoute == AppRoute.Notification.route -> AppRoute.Home.route
        currentRoute == AppRoute.Input.route -> AppRoute.Home.route
        currentRoute == AppRoute.FlowReport.route -> AppRoute.Fortune.route
        currentRoute == AppRoute.ActionPlan.route -> AppRoute.Fortune.route
        currentRoute == AppRoute.Payment.route -> AppRoute.Premium.route
        else -> currentRoute
    }
    // Input and payment are task flows, so the persistent tabs stay out of the way.
    val showBottomNav = !currentRoute.startsWith("reader/") &&
        currentRoute !in setOf(AppRoute.Input.route, AppRoute.Notification.route, AppRoute.Payment.route) &&
        bottomNavRoute in setOf(
            AppRoute.Home.route,
            AppRoute.Fortune.route,
            AppRoute.Library.route,
            AppRoute.Premium.route,
            AppRoute.Settings.route
        )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = bottomNavRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Notification.route) {
                NotificationOnboardingScreen(
                    initialEnabled = uiState.notificationsEnabled,
                    onComplete = { enabled ->
                        viewModel.completeNotificationOnboarding(enabled)
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Notification.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppRoute.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenInput = { navController.navigate(AppRoute.Input.route) },
                    onOpenResult = { navController.navigate(AppRoute.Fortune.route) },
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) },
                    onOpenLibrary = { navController.navigate(AppRoute.Library.route) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                    onOpenBook = { book -> navController.navigateToBook(viewModel, book) }
                )
            }
            composable(AppRoute.Input.route) {
                InputScreen(
                    viewModel = viewModel,
                    onCalculated = { navController.navigate(AppRoute.Fortune.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoute.Fortune.route) {
                ResultScreen(
                    viewModel = viewModel,
                    onOpenFlow = { navController.navigate(AppRoute.FlowReport.route) },
                    onOpenInput = { navController.navigate(AppRoute.Input.route) }
                )
            }
            composable(AppRoute.FlowReport.route) {
                FlowReportScreen(
                    viewModel = viewModel,
                    onOpenActionPlan = { navController.navigate(AppRoute.ActionPlan.route) }
                )
            }
            composable(AppRoute.ActionPlan.route) {
                ActionPlanScreen(
                    viewModel = viewModel,
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) }
                )
            }
            composable(AppRoute.Premium.route) {
                PremiumScreen(
                    viewModel = viewModel,
                    onRequestPersonalConsultation = viewModel::runPremiumConsultation,
                    onOpenBook = { book ->
                        navController.navigateToBook(viewModel, book)
                    },
                    onOpenLibrary = { navController.navigate(AppRoute.Library.route) },
                    onOpenPayment = {
                        if (viewModel.uiState.value.authState is com.example.unum.data.model.AuthState.SignedIn) {
                            navController.navigate(AppRoute.Payment.route)
                        } else {
                            navController.navigate(AppRoute.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable(AppRoute.Payment.route) {
                PaymentScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        if (viewModel.uiState.value.authState !is com.example.unum.data.model.AuthState.SignedIn) {
                            navController.navigate(AppRoute.Settings.route) {
                                launchSingleTop = true
                            }
                        } else {
                            navController.popBackStack(AppRoute.Premium.route, inclusive = false)
                            when (viewModel.uiState.value.premiumMode) {
                                com.example.unum.data.model.PremiumMode.PERSONAL -> viewModel.preparePremiumQuestionConfirmation()
                                com.example.unum.data.model.PremiumMode.COMPATIBILITY -> viewModel.runCompatibilityConsultation()
                            }
                        }
                    }
                )
            }
            composable(AppRoute.Library.route) {
                LibraryScreen(
                    viewModel = viewModel,
                    onOpenBook = { book -> navController.navigateToBook(viewModel, book) }
                )
            }
            composable(AppRoute.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
            composable(
                route = AppRoute.Reader.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                ReaderScreen(
                    viewModel = viewModel,
                    bookId = backStackEntry.arguments?.getString("bookId")
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateToBook(viewModel: AppViewModel, book: FortuneBook) {
    viewModel.selectSavedBook(book)
    navigate(AppRoute.Reader.create(book.bookId))
}
