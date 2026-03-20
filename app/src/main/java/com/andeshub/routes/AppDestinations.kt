package com.andeshub.routes

sealed class AppDestinations(val route: String) {
    object Home      : AppDestinations("home")
    object Catalog   : AppDestinations("catalog")
    object Post      : AppDestinations("post")
    object Favorites : AppDestinations("favorites")
    object Profile   : AppDestinations("profile")
    object Login     : AppDestinations("login")
    object Register  : AppDestinations("register")
    object Store : AppDestinations("store")
    object CreateStore : AppDestinations("create_store")
}
