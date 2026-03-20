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
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import com.andeshub.data.local.SessionManager
import com.andeshub.ui.store.StoreScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val startDestination = if (sessionManager.isLoggedIn()) {
        AppDestinations.Home.route
    } else {
        AppDestinations.Login.route
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = SoftCream,
        bottomBar = {
            if (currentRoute != AppDestinations.Login.route &&
                currentRoute != AppDestinations.Register.route) {
                AndesBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
            composable(AppDestinations.Store.route) {
                StoreScreen(
                    storeName = "My Store",
                    ownerName = "Sofía Ramirez",
                    description = "Description here",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}