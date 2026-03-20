package com.andeshub.routes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andeshub.ui.catalog.CatalogScreen
import com.andeshub.ui.auth.LoginScreen
import com.andeshub.ui.auth.RegisterScreen
import com.andeshub.ui.favorites.FavoritesScreen
import com.andeshub.ui.home.LandingPageScreen
import com.andeshub.ui.navigation.AndesBottomNavBar
import com.andeshub.ui.product.PostProductScreen
import com.andeshub.ui.profile.ProfileScreen
import com.andeshub.ui.theme.SoftCream
import com.andeshub.data.model.Product
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import com.andeshub.data.local.SessionManager
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.ui.store.StoreScreen
import com.andeshub.ui.store.CreateStoreScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    LaunchedEffect(Unit) {
        sessionManager.getAccessToken()?.let {
            RetrofitClient.setToken(it)
        }
    }

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
                CatalogScreen()
            }
            composable(AppDestinations.Post.route) {
                PostProductScreen()
            }
            composable(AppDestinations.Favorites.route) {
                FavoritesScreen()
            }
            composable(AppDestinations.Profile.route) {
                ProfileScreen(
                    onSettingsClick = {},
                    onListingClick = {},
                    onCreateStoreClick = {
                        navController.navigate(AppDestinations.CreateStore.route)
                    },
                    listings = listOf(
                        Product(
                            id = "1",
                            title = "Calculus Textbook",
                            description = "",
                            category = "Books",
                            building_location = "SD",
                            price = 50.0,
                            condition = "Used",
                            image_urls = emptyList(),
                            seller_id = ""
                        ),
                        Product(
                            id = "2",
                            title = "Engineering Drawing Set",
                            description = "",
                            category = "Books",
                            building_location = "SD",
                            price = 30.0,
                            condition = "Used",
                            image_urls = emptyList(),
                            seller_id = ""
                        )
                    )
                )
            }
            composable(AppDestinations.Store.route) {
                StoreScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.CreateStore.route) {
                CreateStoreScreen(
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}