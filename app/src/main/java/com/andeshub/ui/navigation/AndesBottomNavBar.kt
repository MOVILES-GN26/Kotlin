package com.andeshub.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.andeshub.R
import com.andeshub.routes.AppDestinations
import com.andeshub.routes.AppNavigation
import com.andeshub.ui.theme.*

data class NavItem(
    val destination: AppDestinations,
    val label: String,
    val icon: Int
)

@Composable
fun AndesBottomNavBar(navController: NavController) {

    val items = listOf(
        NavItem(AppDestinations.Home,      "Home",      R.drawable.home),
        NavItem(AppDestinations.Catalog,   "Catalog",   R.drawable.catalog),
        NavItem(AppDestinations.Post,      "Post",      R.drawable.post),
        NavItem(AppDestinations.Favorites, "Favorites", R.drawable.favorites),
        NavItem(AppDestinations.Profile,   "Profile",   R.drawable.profile),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.destination.route,
                onClick = {
                    navController.navigate(item.destination.route) {
                        popUpTo(AppDestinations.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    AndesHubTheme {
        AppNavigation()
    }
}