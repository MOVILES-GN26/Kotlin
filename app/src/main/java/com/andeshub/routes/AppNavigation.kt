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
import com.andeshub.ui.favorites.FavoritesViewModel
import com.andeshub.ui.profile.EditProfileScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import com.andeshub.MainActivity
import com.andeshub.ui.product.CheckoutScreen

@Composable
fun AppNavigation(nfcCredentials: StateFlow<Pair<String, String>?> = MutableStateFlow(null)) {
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
                !currentRoute.startsWith("product_detail") &&
                !currentRoute.startsWith("checkout")) {
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
                val credentials by nfcCredentials.collectAsState()
                val authViewModel: com.andeshub.ui.auth.AuthViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()

                val uiState by authViewModel.uiState.collectAsState()

                LaunchedEffect(credentials) {
                    credentials?.let { (email, password) ->
                        authViewModel.login(email, password)
                    }
                }

                LaunchedEffect(uiState) {
                    if (uiState is com.andeshub.ui.auth.AuthUiState.Success) {
                        (context as? MainActivity)?.clearNfcCredentials()
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Login.route) { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    onSignUpClick = {
                        navController.navigate(AppDestinations.Register.route)
                    },
                    onForgotPasswordClick = {},
                    viewModel = authViewModel
                )
            }
            composable(AppDestinations.Register.route) {
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegisterClick = { _, _, _, _ ->
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Register.route) { inclusive = true }
                        }
                    },
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.Home.route) {
                val homeViewModel: com.andeshub.ui.home.HomeViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                LandingPageScreen(
                    viewModel = homeViewModel,
                    onProductClick = { product ->
                        val route = AppDestinations.ProductDetail.createRoute(product.id)
                        navController.navigate(route)
                        navController.getBackStackEntry(route).savedStateHandle["product"] = product
                    }
                )
            }
            composable(AppDestinations.Catalog.route) {
                CatalogScreen(
                    onProductClick = { product ->
                        val route = AppDestinations.ProductDetail.createRoute(product.id)
                        navController.navigate(route)
                        navController.getBackStackEntry(route).savedStateHandle["product"] = product
                    }
                )
            }
            composable(
                route = AppDestinations.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val product = backStackEntry.savedStateHandle.get<Product>("product")
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onBackClick = { navController.popBackStack() },
                        onBuyClick = { selectedProduct ->
                            val route = AppDestinations.Checkout.createRoute(selectedProduct.id)
                            navController.navigate(route)
                            navController.getBackStackEntry(route).savedStateHandle["product"] = selectedProduct
                        }
                    )
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
            composable(
                route = AppDestinations.Checkout.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val product = backStackEntry.savedStateHandle.get<Product>("product")
                if (product != null) {
                    CheckoutScreen(
                        product = product,
                        onBackClick = { navController.popBackStack() },
                        onSubmitProof = { uri ->
                            // Here you would handle the submission to the server
                            navController.popBackStack(AppDestinations.Home.route, false)
                        }
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
                val favoritesViewModel: FavoritesViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onProductClick = { product ->
                        val route = AppDestinations.ProductDetail.createRoute(product.id)
                        navController.navigate(route)
                        navController.getBackStackEntry(route).savedStateHandle["product"] = product
                    }
                )
            }
            composable(AppDestinations.Profile.route) {
                ProfileScreen(
                    onSettingsClick = { navController.navigate(AppDestinations.Settings.route) },
                    onListingClick = { product ->
                        val route = AppDestinations.ProductDetail.createRoute(product.id)
                        navController.navigate(route)
                        navController.getBackStackEntry(route).savedStateHandle["product"] = product
                    },
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
                    onBack = { navController.popBackStack() },
                    onProductClick = { product ->
                        val route = AppDestinations.ProductDetail.createRoute(product.id)
                        navController.navigate(route)
                        navController.getBackStackEntry(route).savedStateHandle["product"] = product
                    }
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
                    onEditProfileClick = {
                        navController.navigate(AppDestinations.EditProfile.route)
                    },
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate(AppDestinations.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppDestinations.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}
