package com.andeshub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andeshub.R
import com.andeshub.ui.components.Category
import com.andeshub.ui.components.CategoryChip
import com.andeshub.ui.components.ProductCard
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.theme.*
import com.andeshub.data.model.Product

@Composable
fun LandingPageScreen(
    viewModel: HomeViewModel = viewModel(),
    onProductClick: (com.andeshub.data.model.Product) -> Unit = {}

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
    ) {
        Text(
            text = "AndesHub",
            style = MaterialTheme.typography.titleLarge,
            color = Black,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.register_image),
                contentDescription = "Hero image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to\nAndesHub",
                    style = MaterialTheme.typography.headlineLarge,
                    color = White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your marketplace for Los Andes students. Buy, sell, and connect with your peers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.search() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Trending Categories",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Sección de Trending Categories Dinámica
        when (uiState) {
            is HomeUiState.Success -> {
                val trending = (uiState as HomeUiState.Success).trendingCategories
                if (trending.isNotEmpty()) {
                    val trendingList = trending.map { tc ->
                        Category(tc.category, getIconForCategory(tc.category))
                    }
                    val rows = trendingList.chunked(3)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { category ->
                                CategoryChip(category = category)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text = "No hay tendencias aún",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedOlive
                    )
                }
            }
            else -> {
                Text(
                    text = "Cargando tendencias...",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedOlive
                )
            }
        }

        // Sección Recently Added
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Recently Added",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Black)
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as HomeUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is HomeUiState.Success -> {
                val products = (uiState as HomeUiState.Success).products
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) }  // ← este es el cambio
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

// SINCRONIZACIÓN DE ICONOS:
// Se mapean exactamente las categorías del Catálogo con sus iconos correspondientes.
fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Books & Supplies" -> Icons.AutoMirrored.Outlined.MenuBook
        "Clothing & Accessories" -> Icons.Outlined.Checkroom
        "Electronics" -> Icons.Outlined.Laptop
        "Food & Drinks" -> Icons.Outlined.Restaurant
        "Furniture" -> Icons.Outlined.Chair
        "Sports & Outdoors" -> Icons.Outlined.SportsSoccer
        "Tickets & Events" -> Icons.Outlined.ConfirmationNumber
        "Transportation" -> Icons.AutoMirrored.Outlined.DirectionsBike
        "Tutoring & Services" -> Icons.Outlined.EditNote
        "Other" -> Icons.Outlined.MoreHoriz
        else -> Icons.Outlined.MoreHoriz
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPageScreenPreview() {
    AndesHubTheme {
        LandingPageScreen()
    }
}
