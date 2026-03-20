package com.andeshub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andeshub.data.model.Product
import com.andeshub.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightNeutral),
            contentAlignment = Alignment.Center
        ) {
            if (product.image_urls.isNotEmpty()) {
                AsyncImage(
                    model = product.image_urls.first().replace("localhost", "10.0.2.2"),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = product.title,
            style = MaterialTheme.typography.titleSmall,
            color = Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$${product.price.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            color = MutedOlive
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    AndesHubTheme {
        ProductCard(
            product = Product(
                id = "1",
                title = "Calculus Textbook",
                description = "Good condition",
                category = "Books",
                building_location = "SD",
                price = 50.0,
                condition = "Used",
                image_urls = emptyList()
            )
        )
    }
}
