package com.andeshub.routes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andeshub.ui.auth.LoginScreen
import com.andeshub.ui.auth.RegisterScreen
import com.andeshub.ui.favorites.FavoritesScreen
import com.andeshub.ui.home.LandingPageScreen
import com.andeshub.ui.navigation.AndesBottomNavBar
import com.andeshub.ui.profile.ProfileScreen
import com.andeshub.ui.theme.SoftCream
import com.andeshub.ui.components.Product

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
            startDestination = AppDestinations.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.Login.route) {
                LoginScreen(
                    onLoginClick = { _, _ ->
                        navController.navigate(AppDestinations.Home.route)
                    },
                    onSignUpClick = {
                        navController.navigate(AppDestinations.Register.route)
                    },
                    onForgotPasswordClick = {}
                )
            }
            composable(AppDestinations.Register.route) {
                RegisterScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRegisterClick = { _, _, _, _ ->
                        navController.navigate(AppDestinations.Home.route)
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
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
                ProfileScreen(
                    onSettingsClick = {},
                    onListingClick = {},
                    listings = listOf(
                        Product("Calculus Textbook", "$50"),
                        Product("Engineering Drawing Set", "$30"),
                        Product("Statistics Software", "$20")
                    )
                )
            }
        }
    }
}