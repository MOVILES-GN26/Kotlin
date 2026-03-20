package com.andeshub.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andeshub.data.model.Product
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.product.ProductUiState
import com.andeshub.ui.product.ProductViewModel
import com.andeshub.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val productViewModel: ProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(context) as T
            }
        }
    )

    val uiState by productViewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCondition by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf("all") }

    LaunchedEffect(searchQuery, selectedCategory, selectedCondition, selectedSort) {
        productViewModel.getProducts(
            search = searchQuery.ifEmpty { null },
            category = selectedCategory,
            condition = selectedCondition,
            priceSort = when(selectedSort) {
                "Lowest Price" -> "asc"
                "Highest Price" -> "desc"
                else -> null
            }
        )
    }

    val products = if (uiState is ProductUiState.Success) {
        (uiState as ProductUiState.Success).products
    } else {
        emptyList()
    }

    val trendingRanking = if (uiState is ProductUiState.Success) {
        (uiState as ProductUiState.Success).trendingCategories.map { it.category }
    } else {
        emptyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "AndesHub", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        sheetType = "all"
                        showFilterSheet = true
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                FilterChip(
                    label = selectedSort ?: "Sort",
                    icon = Icons.Default.KeyboardArrowDown,
                    onClick = {
                        sheetType = "sort"
                        showFilterSheet = true
                    }
                )
                FilterChip(
                    label = selectedCondition ?: "Condition",
                    icon = Icons.Default.KeyboardArrowDown,
                    onClick = {
                        sheetType = "condition"
                        showFilterSheet = true
                    }
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val originalCategories = listOf(
                    "Books & Supplies" to Icons.AutoMirrored.Outlined.MenuBook,
                    "Clothing & Accessories" to Icons.Outlined.Checkroom,
                    "Electronics" to Icons.Outlined.Laptop,
                    "Food & Drinks" to Icons.Outlined.Restaurant,
                    "Furniture" to Icons.Outlined.Chair,
                    "Sports & Outdoors" to Icons.Outlined.SportsSoccer,
                    "Tickets & Events" to Icons.Outlined.ConfirmationNumber,
                    "Transportation" to Icons.AutoMirrored.Outlined.DirectionsBike,
                    "Tutoring & Services" to Icons.Outlined.EditNote,
                    "Other" to Icons.Outlined.MoreHoriz
                )

                val sortedCategories = originalCategories.sortedBy { (label, _) ->
                    val index = trendingRanking.indexOf(label)
                    if (index != -1) index else 100
                }

                items(sortedCategories) { (label, icon) ->
                    CategoryIconItem(
                        label = label,
                        icon = icon,
                        isSelected = selectedCategory == label,
                        onClick = {
                            selectedCategory = if (selectedCategory == label) null else label
                        }
                    )
                }
            }

            when (uiState) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProductUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error: " + (uiState as ProductUiState.Error).message, color = Color.Red)
                            Button(onClick = {
                                productViewModel.getProducts(searchQuery, selectedCategory, selectedCondition, selectedSort)
                            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Text("Retry", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
                else -> {
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No products found", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(products) { product ->
                                CatalogProductItem(product = product, onClick = { onProductClick(product) })
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = null
            ) {
                FilterBottomSheetContent(
                    type = sheetType,
                    selectedCategory = selectedCategory,
                    selectedSort = selectedSort,
                    selectedCondition = selectedCondition,
                    onApply = { cat, sort, cond ->
                        selectedCategory = cat
                        selectedSort = sort
                        selectedCondition = cond
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showFilterSheet = false
                        }
                    },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showFilterSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    type: String,
    selectedCategory: String?,
    selectedSort: String?,
    selectedCondition: String?,
    onApply: (String?, String?, String?) -> Unit,
    onClose: () -> Unit
) {
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempSort by remember { mutableStateOf(selectedSort) }
    var tempCondition by remember { mutableStateOf(selectedCondition) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = when(type) {
                    "sort" -> "Sort by Price"
                    "condition" -> "Condition"
                    else -> "All Filters"
                },
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                tempCategory = null
                tempSort = null
                tempCondition = null
            }) {
                Text("Clear", color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (type == "all" || type == "sort") {
            Text("Sort by Price", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Lowest Price", "Highest Price").forEach { option ->
                    FilterOptionChip(
                        label = option,
                        isSelected = tempSort == option,
                        onClick = { tempSort = if (tempSort == option) null else option }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (type == "all" || type == "condition") {
            Text("Condition", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                listOf("New", "Like New", "Good", "Fair").forEach { cond ->
                    FilterOptionChip(
                        label = cond,
                        isSelected = tempCondition == cond,
                        onClick = { tempCondition = if (tempCondition == cond) null else cond }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (type == "all") {
            Text("Category", style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                val fullCategories = listOf(
                    "Books & Supplies",
                    "Clothing & Accessories",
                    "Electronics",
                    "Food & Drinks",
                    "Furniture",
                    "Sports & Outdoors",
                    "Tickets & Events",
                    "Transportation",
                    "Tutoring & Services",
                    "Other"
                )
                fullCategories.forEach { cat ->
                    FilterOptionChip(
                        label = cat,
                        isSelected = tempCategory == cat,
                        onClick = { tempCategory = if (tempCategory == cat) null else cat }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = { onApply(tempCategory, tempSort, tempCondition) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterOptionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = Typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}

@Composable
fun FilterChip(label: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CategoryIconItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.split(" ")[0],
            style = Typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CatalogProductItem(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD9E8B6))
        ) {
            if (product.image_urls.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = product.image_urls.first().replace("localhost", "10.0.2.2"),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(0.7f)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF6DA025).copy(alpha = 0.6f))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.title,
            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = "$${product.price.toInt()}",
            style = Typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
