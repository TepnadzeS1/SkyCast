package com.sandro.skycast.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun SkyCastNavGraph(navController: NavHostController, viewModel: WeatherViewModel) {
    NavHost(
        navController = navController,
        // APP NOW STARTS ON THE WEATHER VIDEOS
        startDestination = "details"
    ) {
        composable("dashboard") {
            DashboardScreen(navController, viewModel)
        }

        composable("details") {
            WeatherDetailScreen(
                viewModel = viewModel,
                navController = navController,
                onOpenDashboard = {
                    navController.navigate("dashboard") {
                        // This ensures that clicking 'back' from dashboard
                        // doesn't just loop you forever
                        popUpTo("details") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = "map/{cityName}",
            arguments = listOf(navArgument("cityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            PrecipitationMapScreen(
                cityName = cityName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
