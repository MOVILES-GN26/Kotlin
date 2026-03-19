package com.andeshub.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.ui.theme.*

data class FavoriteProduct(
    val title: String,
    val price: String,
    val seller: String
)

@Composable
fun FavoritesScreen() {

    val favorites = listOf(
        FavoriteProduct("Calculator",          "USD 20", "Sofía Ramirez"),
        FavoriteProduct("Calculus Textbook",   "USD 35", "Mateo Vargas"),
        FavoriteProduct("Mac",                 "USD 20", "Sofía Ramirez"),
        FavoriteProduct("Dad Textbook",        "USD 35", "Mateo Vargas"),
        FavoriteProduct("Wireless Headphones", "USD 50", "Isabella Castro"),
        FavoriteProduct("Mechanical Keyboard", "USD 80", "Alejandro Torres"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
    ) {
        // Título
        Text(
            text = "My Favorites",
            style = MaterialTheme.typography.titleLarge,
            color = Black,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 16.dp)
        )

        // Grid de 2 columnas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites) { product ->
                FavoriteCard(product = product)
            }
        }
    }
}

@Composable
fun FavoriteCard(product: FavoriteProduct) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightNeutral)
            .padding(8.dp)
    ) {
        // Imagen placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SoftCream),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📷", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.title,
            style = MaterialTheme.typography.titleSmall,
            color = Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${product.price} · Seller: ${product.seller}",
            style = MaterialTheme.typography.labelMedium,
            color = MutedOlive,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    AndesHubTheme {
        FavoritesScreen()
    }
}