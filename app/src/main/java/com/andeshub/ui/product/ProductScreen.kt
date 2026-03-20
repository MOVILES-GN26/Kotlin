package com.andeshub.ui.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Product Details",
                        style = Typography.titleLarge,
                        color = Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { /* TODO: Buy Now action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Buy Now",
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { /* TODO: WhatsApp action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, LightNeutral)
                ) {
                    Text(
                        text = "Contact Seller via WhatsApp",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Black
                    )
                }
            }
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFD9E8B6))
            ) {
                if (product.image_urls.isNotEmpty()) {
                    AsyncImage(
                        model = product.image_urls.first().replace("localhost", "10.0.2.2"),
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback placeholder artistic style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(0.7f)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF6DA025).copy(alpha = 0.8f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(100.dp)
                            .align(Alignment.Center)
                            .background(Color(0xFF5D5438).copy(alpha = 0.7f))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = product.title,
                    style = Typography.headlineLarge,
                    color = Black
                )
                Text(
                    text = "$${product.price.toInt()}",
                    style = Typography.titleLarge,
                    color = MutedOlive,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección del Vendedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Seller Information",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Black
                        )
                        Text(
                            text = product.seller?.name ?: "Unknown Seller",
                            style = Typography.labelMedium,
                            color = MutedOlive
                        )
                        product.seller?.major?.let {
                            Text(
                                text = "Major: $it",
                                style = Typography.labelSmall,
                                color = MutedOlive
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "View Profile",
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Black
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(start = 4.dp),
                                tint = Black
                            )
                        }
                    }

                    // Avatar del vendedor
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF9F5D7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Seller Avatar",
                            tint = Color(0xFF5D5438),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ubicación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MutedOlive,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.building_location,
                        style = Typography.bodyMedium,
                        color = MutedOlive
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Descripción
                Text(
                    text = "Description",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = Typography.bodyMedium,
                    color = MutedOlive,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailPreview() {
    AndesHubTheme {
        ProductDetailScreen(
            product = Product(
                id = "1",
                title = "Tomi verde",
                description = "Verde verde",
                category = "Other",
                building_location = "Biblioteca General",
                price = 15000.0,
                condition = "NEW",
                image_urls = emptyList(),
                seller_id = "s1",
                seller = UserProfile(
                    id = "s1",
                    name = "Sofia Rozo",
                    major = "Ingeniería de Sistemas y Computación"
                )
            )
        )
    }
}
