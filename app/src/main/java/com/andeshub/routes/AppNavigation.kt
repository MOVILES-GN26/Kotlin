package com.andeshub.routes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andeshub.ui.favorites.FavoritesScreen
import com.andeshub.ui.home.LandingPageScreen
import com.andeshub.ui.navigation.AndesBottomNavBar
import com.andeshub.ui.profile.ProfileScreen
import com.andeshub.ui.theme.SoftCream

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = SoftCream,
        bottomBar = {
            AndesBottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.Home.route) {
                LandingPageScreen()
            }
            composable(AppDestinations.Catalog.route) {
                // TODO: CatalogScreen()
            }
            composable(AppDestinations.Post.route) {
                // TODO: PostScreen()
            }
            composable(AppDestinations.Favorites.route) {
                FavoritesScreen()
            }
            composable(AppDestinations.Profile.route) {
                ProfileScreen(onSettingsClick = {},
                    onListingClick = {})
            }
        }
    }
}