package com.example.unum.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.Scaffold
import com.example.unum.ads.AdMobBanner
import com.example.unum.ads.InterstitialAdManager
import com.example.unum.ads.RewardedAdManager
import com.example.unum.presentation.AppViewModel
import com.example.unum.presentation.HomeScreen
import com.example.unum.presentation.InputScreen
import com.example.unum.presentation.LibraryScreen
import com.example.unum.presentation.PremiumScreen
import com.example.unum.presentation.ReaderScreen
import com.example.unum.presentation.ResultScreen
import com.example.unum.presentation.SettingsScreen
import com.example.unum.ui.components.BottomNavBar
import com.example.unum.data.model.FortuneBook

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Input : AppRoute("input")
    data object Fortune : AppRoute("fortune")
    data object Premium : AppRoute("premium")
    data object Library : AppRoute("library")
    data object Settings : AppRoute("settings")
    data object Reader : AppRoute("reader/{bookId}") {
        fun create(bookId: String) = "reader/$bookId"
    }
}

@Composable
fun UnumAppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoute.Home.route
    val activity = LocalContext.current as? Activity
    val interstitialAdManager = remember(activity) { activity?.let { InterstitialAdManager(it) } }
    val rewardedAdManager = remember(activity) { activity?.let { RewardedAdManager(it) } }

    LaunchedEffect(interstitialAdManager) {
        interstitialAdManager?.load()
    }
    LaunchedEffect(rewardedAdManager) {
        rewardedAdManager?.load()
    }

    Scaffold(
        bottomBar = {
            Column {
                AdMobBanner()
                BottomNavBar(
                    currentRoute = when {
                        currentRoute.startsWith("reader/") -> AppRoute.Library.route
                        currentRoute == AppRoute.Input.route -> AppRoute.Home.route
                        else -> currentRoute
                    },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
            composable(AppRoute.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenInput = { navController.navigate(AppRoute.Input.route) },
                    onOpenResult = { navController.navigate(AppRoute.Fortune.route) },
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) },
                    onOpenBook = { book -> navController.navigateToBook(viewModel, book) }
                )
            }
            composable(AppRoute.Input.route) {
                InputScreen(
                    viewModel = viewModel,
                    onCalculated = {
                        interstitialAdManager?.showOrContinue {
                            navController.navigate(AppRoute.Fortune.route)
                        } ?: navController.navigate(AppRoute.Fortune.route)
                    }
                )
            }
            composable(AppRoute.Fortune.route) {
                ResultScreen(
                    viewModel = viewModel,
                    onOpenPremium = { navController.navigate(AppRoute.Premium.route) }
                )
            }
            composable(AppRoute.Premium.route) {
                PremiumScreen(
                    viewModel = viewModel,
                    onRequestPersonalConsultation = {
                        rewardedAdManager?.showOrContinue(viewModel::runPremiumConsultation)
                            ?: viewModel.runPremiumConsultation()
                    },
                    onRequestCompatibilityConsultation = {
                        rewardedAdManager?.showOrContinue(viewModel::runCompatibilityConsultation)
                            ?: viewModel.runCompatibilityConsultation()
                    },
                    onOpenBook = { book ->
                        navController.navigateToBook(viewModel, book)
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
