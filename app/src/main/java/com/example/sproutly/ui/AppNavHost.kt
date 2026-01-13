package com.example.sproutly.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sproutly.network.UserRemote
import com.example.sproutly.ui.components.CreateGardenScreen
import com.example.sproutly.ui.components.GardenDetailScreen
import com.example.sproutly.ui.components.HomeScreen
import com.example.sproutly.utils.SproutlyDestinations

@Composable
fun AppNavHost(currentUser: UserRemote) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SproutlyDestinations.HOME
    ) {
        composable(SproutlyDestinations.HOME) {
            HomeScreen(
                currentUser = currentUser,
                onAddClick = { navController.navigate(SproutlyDestinations.CREATE_GARDEN) },
                onGardenClick = { gardenId ->
                    navController.navigate("${SproutlyDestinations.GARDEN_DETAIL}/$gardenId")
                }
            )
        }

        composable(
            route = "${SproutlyDestinations.GARDEN_DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            GardenDetailScreen(
                gardenId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SproutlyDestinations.CREATE_GARDEN) {
            CreateGardenScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }
    }
}
