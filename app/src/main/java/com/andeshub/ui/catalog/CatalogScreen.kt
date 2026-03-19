package com.andeshub.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Product) -> Unit = {}
) {
    val products = listOf(
        Product(
            id = "1",
            title = "Scientific Calculator",
            description = "Casio FX-991ES Plus",
            category = "Electronics",
            building_location = "Edificio Mario Laserna",
            price = 25000.0,
            condition = "USED",
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
            condition = "NEW",
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
            condition = "USED",
            image_urls = listOf(),
            seller_id = "s3",
            seller = UserProfile("s3", "Andres Felipe", "Administración")
        ),
        Product(
            id = "4",
            title = "Engineering Book",
            description = "Thermodynamics 8th Edition",
            category = "Books",
            building_location = "Edificio Santo Domingo",
            price = 35000.0,
            condition = "USED",
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
            condition = "NEW",
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
            condition = "NEW",
            image_urls = listOf(),
            seller_id = "s6",
            seller = UserProfile("s6", "Carlos Ruiz", "Arquitectura")
        )
    )

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
            // Search Bar
            SearchBar(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(label = "Sort", icon = Icons.Default.KeyboardArrowDown)
                FilterChip(label = "Condition", icon = Icons.Default.KeyboardArrowDown)
            }

            // Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryIconItem("Books", Icons.AutoMirrored.Outlined.MenuBook)
                CategoryIconItem("Clothing", Icons.Outlined.Checkroom)
                CategoryIconItem("Electronics", Icons.Outlined.Laptop)
                CategoryIconItem("Food", Icons.Outlined.Restaurant)
                CategoryIconItem("Furniture", Icons.Outlined.Chair)
            }

            // Products Grid
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

@Composable
fun FilterChip(label: String, icon: ImageVector) {
    Surface(
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
fun CategoryIconItem(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(SoftCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Black, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = Typography.labelSmall, color = MutedOlive)
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
            // Placeholder boxes to mimic the pixel art style
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
