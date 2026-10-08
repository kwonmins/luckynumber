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
import androidx.compose.ui.platform.LocalContext
import com.example.unum.presentation.discovery.*
import com.example.unum.data.content.TarotCatalog
import com.example.unum.data.model.PremiumMode
import java.time.LocalDate
import com.example.unum.data.model.FortuneBook
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.home.HomeScreen
import com.example.unum.presentation.input.InputScreen
import com.example.unum.presentation.library.LibraryScreen
import com.example.unum.presentation.onboarding.NotificationOnboardingScreen
import com.example.unum.presentation.payment.PaymentScreen
import com.example.unum.presentation.premium.PremiumScreen
import com.example.unum.presentation.reader.ReaderScreen
import com.example.unum.presentation.result.ResultScreen
import com.example.unum.presentation.settings.SettingsScreen
import com.example.unum.ui.components.BottomNavBar

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Notification : AppRoute("notification")
    data object Input : AppRoute("input")
    data object Fortune : AppRoute("fortune")
    data object Explore : AppRoute("explore")
    data object Benefits : AppRoute("benefits")
    data object Tarot : AppRoute("tarot")
    data object TarotHistory : AppRoute("tarot_history")
    data object History : AppRoute("history")
    data object Monthly : AppRoute("monthly")
    data object Numbers : AppRoute("numbers")
    data object Luck : AppRoute("luck")
    data object DatedFortune : AppRoute("day/{date}")
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
    val context=LocalContext.current
    fun openDate(date: LocalDate) { navController.navigate("day/$date") {launchSingleTop=true} }
    fun openFeature(feature: String) {
        when(feature) {
            "today" -> navController.navigate(AppRoute.Fortune.route)
            "tomorrow" -> openDate(TarotCatalog.today().plusDays(1))
            "date" -> chooseFortuneDate(context,::openDate)
            "compatibility" -> {viewModel.resetPremiumFlow();viewModel.setPremiumMode(PremiumMode.COMPATIBILITY);navController.navigate(AppRoute.Premium.route)}
            "tarotHistory" -> navController.navigate(AppRoute.TarotHistory.route)
            else -> navController.navigate(feature) {launchSingleTop=true}
        }
    }

    val bottomNavRoute = when {
        currentRoute.startsWith("reader/") || currentRoute == AppRoute.Library.route || currentRoute == AppRoute.History.route || currentRoute == AppRoute.TarotHistory.route -> AppRoute.Settings.route
        currentRoute.startsWith("day/") || currentRoute in setOf(AppRoute.Fortune.route,AppRoute.Tarot.route,AppRoute.Monthly.route,AppRoute.Numbers.route,AppRoute.Luck.route) -> AppRoute.Explore.route
        currentRoute == AppRoute.Notification.route -> AppRoute.Home.route
        currentRoute == AppRoute.Input.route -> AppRoute.Home.route
        currentRoute == AppRoute.Payment.route -> AppRoute.Premium.route
        else -> currentRoute
    }
    // Input and payment are task flows, so the persistent tabs stay out of the way.
    val showBottomNav = !currentRoute.startsWith("reader/") &&
        currentRoute !in setOf(AppRoute.Input.route, AppRoute.Notification.route, AppRoute.Payment.route) &&
        bottomNavRoute in setOf(
            AppRoute.Home.route,
            AppRoute.Explore.route,
            AppRoute.Benefits.route,
            AppRoute.Premium.route,
            AppRoute.Settings.route
        )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = bottomNavRoute,
                    dark = bottomNavRoute == AppRoute.Premium.route,
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
                    onOpenToday = { navController.navigate(AppRoute.Fortune.route) },
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) },
                    onOpenLibrary = { navController.navigate(AppRoute.Library.route) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                    onOpenFeature = ::openFeature,
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
            composable(AppRoute.Explore.route) { DiscoveryScreen(viewModel,::openFeature) }
            composable(AppRoute.Benefits.route) { BenefitsScreen(viewModel,{openFeature("tarot")},{openFeature("tarotHistory")}) }
            composable(AppRoute.Tarot.route) { TarotScreen(viewModel,{navController.popBackStack()},{openFeature("settings")}) }
            composable(AppRoute.TarotHistory.route) { TarotHistoryScreen(viewModel,{navController.popBackStack()},{openFeature("tarot")}) }
            composable(AppRoute.History.route) { HistoryScreen(viewModel,{navController.popBackStack()},::openDate,{openFeature("tarotHistory")},{openFeature("library")}) }
            composable(AppRoute.Monthly.route) { MonthlyScreen(viewModel,{navController.popBackStack()},{openFeature("input")},::openDate) }
            composable(AppRoute.Numbers.route) { NumerologyScreen(viewModel,{navController.popBackStack()},{openFeature("input")}) }
            composable(AppRoute.Luck.route) { ResultScreen(viewModel,{openFeature("input")},{openFeature("premium")},onlyLuck=true) }
            composable(AppRoute.DatedFortune.route,arguments=listOf(navArgument("date") {type=NavType.StringType})) { entry ->
                ResultScreen(viewModel,{openFeature("input")},{openFeature("premium")},date=entry.arguments?.getString("date")?.let {runCatching {LocalDate.parse(it)}.getOrNull()} ?: TarotCatalog.today())
            }
            composable(AppRoute.Fortune.route) {
                ResultScreen(
                    viewModel = viewModel,
                    onOpenInput = { navController.navigate(AppRoute.Input.route) },
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) }
                )
            }
            composable(AppRoute.Premium.route) {
                PremiumScreen(
                    viewModel = viewModel,
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
                                com.example.unum.data.model.PremiumMode.PERSONAL -> viewModel.runPremiumConsultation()
                                com.example.unum.data.model.PremiumMode.COMPATIBILITY -> viewModel.runCompatibilityConsultation()
                            }
                        }
                    }
                )
            }
            composable(AppRoute.Library.route) {
                LibraryScreen(
                    viewModel = viewModel,
                    onOpenBook = { book -> navController.navigateToBook(viewModel, book) },
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) }
                )
            }
            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onOpenLibrary = {openFeature("library")},
                    onOpenHistory = {openFeature("history")},
                    onSignedOut = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
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
