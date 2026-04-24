package com.andeshub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    onProductClick: (Product) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // LazyVerticalGrid no puede tener contenido scrollable dentro de Column,
    // entonces usamos un solo LazyVerticalGrid con headers como items
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header: título AndesHub
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                text = "AndesHub",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Header: Hero image con búsqueda
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
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
        }

        // Header: Trending Categories título
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Trending Categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Header: chips de categorías
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            when (uiState) {
                is HomeUiState.Success -> {
                    val trending = (uiState as HomeUiState.Success).trendingCategories
                    if (trending.isNotEmpty()) {
                        val trendingList = trending.map { tc ->
                            Category(tc.category, getIconForCategory(tc.category))
                        }
                        val rows = trendingList.chunked(3)
                        Column {
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { category ->
                                        CategoryChip(
                                            category = category,
                                            isSelected = selectedCategory == category.label,
                                            onClick = { viewModel.onCategorySelected(category.label) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
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
        }

        // Header: Recently Added título
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recently Added",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Contenido: productos o loading/error
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            is HomeUiState.Error -> {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            is HomeUiState.Success -> {
                val products = state.products.filter { product ->
                    val matchesSearch = searchQuery.isEmpty() ||
                            product.title.contains(searchQuery, ignoreCase = true) ||
                            product.description.contains(searchQuery, ignoreCase = true)
                    val matchesCategory = selectedCategory == null ||
                            product.category == selectedCategory
                    matchesSearch && matchesCategory
                }

                if (products.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No products found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                } else {
                    items(products) { product ->
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product) }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

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