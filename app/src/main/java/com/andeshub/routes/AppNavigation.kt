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
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.product.ProductDetailScreen
import androidx.compose.runtime.getValue
import com.andeshub.ui.onboarding.OnboardingScreen
import com.andeshub.ui.store.CreateStoreScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.andeshub.ui.settings.SettingsScreen

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

    val startDestination = if (!sessionManager.isOnboardingCompleted()) {
        AppDestinations.Onboarding.route
    } else if (sessionManager.isLoggedIn()) {
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
                currentRoute != AppDestinations.Onboarding.route &&
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
            composable(AppDestinations.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        sessionManager.setOnboardingCompleted()
                        navController.navigate(AppDestinations.Login.route) {
                            popUpTo(AppDestinations.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppDestinations.Login.route) {
                LoginScreen(
                    onLoginClick = { _, _ ->
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Login.route) { inclusive = true }
                        }
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
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Register.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(AppDestinations.Home.route) {
                val homeViewModel: com.andeshub.ui.home.HomeViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                LandingPageScreen(
                    viewModel = homeViewModel,
                    onProductClick = { product ->
                        // Guarda en el backstack del ProductDetail, no del Home
                        navController.navigate(AppDestinations.ProductDetail.createRoute(product.id))
                        navController.getBackStackEntry(AppDestinations.ProductDetail.createRoute(product.id))
                            .savedStateHandle["product"] = product
                    }
                )
            }
            composable(AppDestinations.Catalog.route) {
                CatalogScreen(
                    onProductClick = { product ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("product", product)
                        navController.navigate(AppDestinations.ProductDetail.route.replace("{productId}", product.id))
                    }
                )
            }
            composable(AppDestinations.ProductDetail.route) { backStackEntry ->
                val product = backStackEntry.savedStateHandle.get<Product>("product")
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
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
                    onSettingsClick = {navController.navigate(AppDestinations.Settings.route)},
                    onListingClick = { _: String -> },
                    onCreateStoreClick = {
                        navController.navigate(AppDestinations.CreateStore.route)
                    },
                    onStoreClick = { storeId ->
                        navController.navigate(AppDestinations.StoreDetail.createRoute(storeId))
                    }
                )
            }
            composable(
                route = AppDestinations.StoreDetail.route,
                arguments = listOf(navArgument("storeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                StoreScreen(
                    storeId = storeId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.CreateStore.route) {
                CreateStoreScreen(
                    onClose = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate(AppDestinations.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
