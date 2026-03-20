package com.andeshub.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.ui.components.Product
import com.andeshub.ui.components.ProductCard
import com.andeshub.ui.theme.*

@Composable
fun StoreScreen(
    storeName: String = "Store name",
    ownerName: String = "Owner",
    description: String = "Description",
    onBack: () -> Unit = {}
) {
    val products = listOf(
        Product("Calculus Textbook", "\$50"),
        Product("MacBook Pro",       "\$1200"),
        Product("Calculus Textbook", "\$50"),
        Product("MacBook Pro",       "\$1200"),
    )

    val chunkedProducts = products.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Black
                )
            }
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleLarge,
                color = Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Logo centrado
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightNeutral),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Logo tienda",
                            tint = MutedOlive,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Dueño
                Text(
                    text = ownerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedOlive,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Descripción
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall,
                    color = Black,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Products título
                Text(
                    text = "Products",
                    style = MaterialTheme.typography.titleSmall,
                    color = Black,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Grid de productos
            items(chunkedProducts.size) { index ->
                val row = chunkedProducts[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { product ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(product = product)
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoreScreenPreview() {
    AndesHubTheme {
        StoreScreen(
            storeName = "My Store",
            ownerName = "Sofía Ramirez",
            description = "This textbook is in excellent condition and covers all the essential topics in engineering physics."
        )
    }
}