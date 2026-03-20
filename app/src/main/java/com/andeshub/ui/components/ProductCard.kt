package com.andeshub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andeshub.data.model.Product
import com.andeshub.data.model.ProductStats
import com.andeshub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductCard(
    product: Product,
    stats: ProductStats? = null,
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
                .background(MaterialTheme.colorScheme.surface),
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

            // Badge de última interacción
            stats?.last_viewed?.let { lastViewed ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = formatTimeAgo(lastViewed),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall,
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            if (stats != null && stats.views > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MutedOlive,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${stats.views}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MutedOlive
                    )
                }
            }
        }
        
        Text(
            text = "$${product.price.toInt()}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MutedOlive
        Text(
            text = product.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$${product.price.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

fun formatTimeAgo(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString) ?: return "Just now"
        val now = Date()
        val diff = now.time - date.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            else -> "${days}d ago"
        }
    } catch (e: Exception) {
        "Recently"
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
            ),
            stats = ProductStats(views = 15, last_viewed = "2023-10-27T10:00:00.000Z", last_user_id = null)
        )
    }
}