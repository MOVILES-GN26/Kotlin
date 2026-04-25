package com.andeshub.ui.product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.andeshub.data.model.Product
import com.andeshub.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    product: Product,
    onBackClick: () -> Unit = {},
    onSubmitProof: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(context) as T
            }
        }
    )

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    // ESTRATEGIA: MONITOREO DE CONEXIÓN PROACTIVO
    val isOnline = remember { mutableStateOf(viewModel.isNetworkAvailable()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            isOnline.value = viewModel.isNetworkAvailable()
            delay(2000) // Verifica cada 2 segundos
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Complete Purchase",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!isOnline.value) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "You are offline. Connectivity is required to complete the purchase.",
                        modifier = Modifier.padding(12.dp),
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Product Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$${product.price.toInt()}",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(100.dp, 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5D6B6))
                ) {
                    if (product.image_urls.isNotEmpty()) {
                        AsyncImage(
                            model = product.image_urls.first().replace("localhost", "10.0.2.2"),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Proof of Payment",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = if (isOnline.value) MaterialTheme.colorScheme.outlineVariant else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(if (isOnline.value) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.1f))
                    .clickable(enabled = isOnline.value) { 
                        galleryLauncher.launch("image/*") 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isOnline.value) "Upload Receipt/Screenshot" else "Offline - Upload Disabled",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline.value) MaterialTheme.colorScheme.onBackground else Color.Gray
                        )
                        Text(
                            text = "Comprobante de Pago",
                            style = Typography.bodySmall,
                            color = if (isOnline.value) MaterialTheme.colorScheme.secondary else Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isOnline.value) MaterialTheme.colorScheme.surface else Color.Gray.copy(alpha = 0.3f),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                "Upload",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = Typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline.value) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Proof",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Transfer the money to the seller's account.",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NEQUI: 3222222222",
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "NU : 0197247724",
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { 
                    if (viewModel.isNetworkAvailable()) {
                        imageUri?.let { onSubmitProof(it) }
                    } else {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        isOnline.value = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = imageUri != null && isOnline.value,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline.value) Color(0xFFE6E64B) else Color.Gray,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (isOnline.value) "Submit Proof" else "Offline - Cannot Submit",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
