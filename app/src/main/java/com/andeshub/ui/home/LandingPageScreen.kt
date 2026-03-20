package com.andeshub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.R
import com.andeshub.data.model.Product
import com.andeshub.ui.components.Category
import com.andeshub.ui.components.CategoryChip
import com.andeshub.ui.components.ProductCard
import com.andeshub.ui.components.SearchBar
import com.andeshub.ui.theme.*

@Composable
fun LandingPageScreen() {

    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        Category("Books",    Icons.Outlined.MenuBook),
        Category("Tech",     Icons.Outlined.Laptop),
        Category("Housing",  Icons.Outlined.Home),
        Category("Services", Icons.Outlined.Build),
        Category("Events",   Icons.Outlined.Event),
        Category("Other",    Icons.Outlined.MoreHoriz)
    )

    val products = listOf(
        Product("Calculus Textbook", "$50"),
        Product("MacBook Pro", "$1200"),
        Product("Apartment campus", "$800/mo"),
        Product("Physics Book", "$30"),
        Product(
            id = "1",
            title = "Calculus Textbook",
            description = "",
            category = "Books",
            building_location = "SD",
            price = 50.0,
            condition = "Used",
            image_urls = emptyList(),
            seller_id = ""
        ),
        Product(
            id = "2",
            title = "MacBook Pro",
            description = "",
            category = "Tech",
            building_location = "SD",
            price = 1200.0,
            condition = "Used",
            image_urls = emptyList(),
            seller_id = ""
        ),
        Product(
            id = "3",
            title = "Apartment campus",
            description = "",
            category = "Housing",
            building_location = "SD",
            price = 800.0,
            condition = "New",
            image_urls = emptyList(),
            seller_id = ""
        ),
        Product(
            id = "4",
            title = "Physics Book",
            description = "",
            category = "Books",
            building_location = "SD",
            price = 30.0,
            condition = "Used",
            image_urls = emptyList(),
            seller_id = ""
        ),
    )

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
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* TODO */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        val rows = categories.chunked(3)
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

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Recently Added",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(product = product)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPageScreenPreview() {
    AndesHubTheme {
        LandingPageScreen()
    }
}