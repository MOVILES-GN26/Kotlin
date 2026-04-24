package com.andeshub.routes

sealed class AppDestinations(val route: String) {
    object Onboarding : AppDestinations("onboarding")
    object Home      : AppDestinations("home")
    object Catalog   : AppDestinations("catalog")
    object Post      : AppDestinations("post")
    object Favorites : AppDestinations("favorites")
    object Profile   : AppDestinations("profile")
    object Login     : AppDestinations("login")
    object Register  : AppDestinations("register")
    object Store : AppDestinations("store")
    object CreateStore : AppDestinations("create_store")
    object ProductDetail : AppDestinations("product_detail/{productId}"){
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    object StoreDetail : AppDestinations("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }

    object Settings : AppDestinations("settings")

    object EditProfile : AppDestinations("edit_profile")

    object Checkout : AppDestinations("checkout/{productId}") {
        fun createRoute(productId: String) = "checkout/$productId"
    }
}
