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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Product) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCondition by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf<String?>(null) } // "Lowest", "Highest"

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf("all") } // "all", "sort", "condition"

    val products = listOf(
        Product(
            id = "1",
            title = "Scientific Calculator",
            description = "Casio FX-991ES Plus",
            category = "Electronics",
            building_location = "Edificio Mario Laserna",
            price = 25000.0,
            condition = "Used",
            image_urls = listOf(),
            seller_id = "s1",
            seller = UserProfile("s1", "Juan Perez", "Ingeniería Mecánica")
        ),
        Product(
            id = "2",
            title = "Smartphone Case",
            description = "Silicone case for iPhone 13",
            category = "Electronics",
            building_location = "Centro del Japón",
            price = 10000.0,
            condition = "New",
            image_urls = listOf(),
            seller_id = "s2",
            seller = UserProfile("s2", "Maria Garcia", "Diseño")
        ),
        Product(
            id = "3",
            title = "AirPods Pro",
            description = "Excellent condition, original",
            category = "Electronics",
            building_location = "Biblioteca General",
            price = 450000.0,
            condition = "Like New",
            image_urls = listOf(),
            seller_id = "s3",
            seller = UserProfile("s3", "Andres Felipe", "Administración")
        ),
        Product(
            id = "4",
            title = "Engineering Book",
            description = "Thermodynamics 8th Edition",
            category = "Books & Supplies",
            building_location = "Edificio Santo Domingo",
            price = 35000.0,
            condition = "Good",
            image_urls = listOf(),
            seller_id = "s4",
            seller = UserProfile("s4", "Laura Torres", "Ingeniería Química")
        ),
        Product(
            id = "5",
            title = "Tomi verde",
            description = "Verde verde",
            category = "Other",
            building_location = "Biblioteca General",
            price = 15000.0,
            condition = "New",
            image_urls = listOf(),
            seller_id = "s5",
            seller = UserProfile("s5", "Sofia Rozo", "Ingeniería de Sistemas")
        ),
        Product(
            id = "6",
            title = "Laptop Stand",
            description = "Aluminum foldable stand",
            category = "Electronics",
            building_location = "Cafetería Central",
            price = 20000.0,
            condition = "Fair",
            image_urls = listOf(),
            seller_id = "s6",
            seller = UserProfile("s6", "Carlos Ruiz", "Arquitectura")
        )
    )

    val filteredAndSortedProducts = products
        .filter { 
            searchQuery.isEmpty() || 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true) 
        }
        .filter { selectedCategory == null || it.category.equals(selectedCategory, ignoreCase = true) }
        .filter { selectedCondition == null || it.condition.equals(selectedCondition, ignoreCase = true) }
        .let { list ->
            when (selectedSort) {
                "Lowest Price" -> list.sortedBy { it.price }
                "Highest Price" -> list.sortedByDescending { it.price }
                else -> list
            }
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AndesHub",
                        style = Typography.titleLarge,
                        color = Black
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        containerColor = White
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

            // Filter Chips
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
                        .background(SoftCream, RoundedCornerShape(8.dp))
                        .border(1.dp, LightNeutral, RoundedCornerShape(8.dp))
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

            // Categories - Now Scrollable (LazyRow)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val scrollableCategories = listOf(
                    "Books & Supplies" to Icons.AutoMirrored.Outlined.MenuBook,
                    "Clothing & Accessories" to Icons.Outlined.Checkroom,
                    "Electronics" to Icons.Outlined.Laptop,
                    "Food & Drinks" to Icons.Outlined.Restaurant,
                    "Furniture" to Icons.Outlined.Chair,
                    "Sports & Outdoors" to Icons.Outlined.SportsSoccer,
                    "Tickets & Events" to Icons.Outlined.ConfirmationNumber,
                    "Transportation" to Icons.Outlined.DirectionsBike,
                    "Tutoring & Services" to Icons.Outlined.EditNote,
                    "Other" to Icons.Outlined.MoreHoriz
                )

                items(scrollableCategories) { (label, icon) ->
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

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredAndSortedProducts) { product ->
                    CatalogProductItem(product = product, onClick = { onProductClick(product) })
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = SoftCream,
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
                Text("Clear", color = MutedOlive)
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
            colors = ButtonDefaults.buttonColors(containerColor = Yellow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply", color = Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterOptionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Yellow.copy(alpha = 0.2f) else White,
        border = BorderStroke(1.dp, if (isSelected) Yellow else LightNeutral),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = Typography.labelMedium,
            color = Black
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
        color = SoftCream,
        border = BorderStroke(1.dp, LightNeutral),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = Typography.labelMedium, color = Black)
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Black)
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
                .background(if (isSelected) Yellow else SoftCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.split(" ")[0], // Use first word for a cleaner look in the scroll bar
            style = Typography.labelSmall,
            color = if (isSelected) Black else MutedOlive,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight(0.7f)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF6DA025).copy(alpha = 0.6f))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.title,
            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Black,
            maxLines = 1
        )
        Text(
            text = "$${product.price.toInt()}",
            style = Typography.labelMedium,
            color = MutedOlive
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogPreview() {
    AndesHubTheme {
        CatalogScreen()
    }
}