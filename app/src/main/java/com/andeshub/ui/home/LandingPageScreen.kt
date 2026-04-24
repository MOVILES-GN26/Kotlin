package com.andeshub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.andeshub.data.model.Product
import com.andeshub.ui.components.Category
import com.andeshub.ui.components.CategoryChip
import com.andeshub.ui.components.ProductCard
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.theme.*

@Composable
fun LandingPageScreen(
    viewModel: HomeViewModel = viewModel(),
    onProductClick: (Product) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val viewedTimestamps by viewModel.viewedTimestamps.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    var showHistory by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        viewModel.loadViewedTimestamps()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showHistory = false
            }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
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

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.register_image),
                        contentDescription = "Hero image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 20.dp)
                            .padding(top = 32.dp),
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
                            onQueryChange = {
                                viewModel.onSearchQueryChange(it)
                                showHistory = it.isEmpty()
                            },
                            onSearch = {
                                viewModel.search()
                                showHistory = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHistory = searchHistory.isNotEmpty() }
                        )

                        if (showHistory && searchHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Column {
                                    searchHistory.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectHistoryItem(item)
                                                    showHistory = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                                    showStats = false,
                                    onClick = { onProductClick(product) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    } // cierre del Box exterior
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
fun LandingPagePreview() {
    AndesHubTheme {
        LandingPageScreen()
    }
}