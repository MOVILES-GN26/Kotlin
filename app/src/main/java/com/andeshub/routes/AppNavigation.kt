package com.andeshub.routes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.andeshub.ui.store.StoreScreen
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.product.ProductDetailScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect

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
            if (currentRoute != null && 
                currentRoute != AppDestinations.Login.route &&
                currentRoute != AppDestinations.Register.route &&
                !currentRoute.startsWith("product_detail")) {
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
                CatalogScreen(
                    onProductClick = { product ->
                        // Pasamos el objeto completo a través del savedStateHandle
                        navController.currentBackStackEntry?.savedStateHandle?.set("product", product)
                        navController.navigate(AppDestinations.ProductDetail.route.replace("{productId}", product.id))
                    }
                )
            }
            composable(AppDestinations.ProductDetail.route) { backStackEntry ->
                // Recuperamos el producto del savedStateHandle de la entrada anterior
                val product = navController.previousBackStackEntry?.savedStateHandle?.get<Product>("product")
                
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    // Si por alguna razón es nulo, volvemos atrás
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
            composable(AppDestinations.Post.route) {
                val userProfile = UserProfile(
                    id = sessionManager.getUserId() ?: "",
                    name = "${sessionManager.getUserFirstName()} ${sessionManager.getUserLastName()}",
                    email = sessionManager.getUserEmail() ?: "",
                    major = sessionManager.getUserMajor() ?: ""
                )
                PostProductScreen(
                    currentUser = userProfile,
                    onCloseClick = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.Favorites.route) {
                FavoritesScreen()
            }
            composable(AppDestinations.Profile.route) {
                ProfileScreen(
                    onSettingsClick = {},
                    onListingClick = {},
                    listings = emptyList()
                )
            }
            composable(AppDestinations.Store.route) {
                StoreScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
