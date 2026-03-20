package com.andeshub.ui.product

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.andeshub.data.model.Store
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProductScreen(
    currentUser: UserProfile? = null,
    onCloseClick: () -> Unit = {}
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
    val userStores by productViewModel.userStores.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var buildingLocation by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedStore by remember { mutableStateOf<Store?>(null) }

    val categories = listOf(
        "Books & Supplies", "Clothing & Accessories", "Electronics", 
        "Food & Drinks", "Furniture", "Sports & Outdoors", 
        "Tickets & Events", "Transportation", "Tutoring & Services", "Other"
    )
    val conditions = listOf("New", "Like New", "Good", "Fair")
    val buildings = listOf("Mario Laserna", "Santo Domingo", "Centro del Japón", "Biblioteca General", "Cafetería Central")

    var categoryExpanded by remember { mutableStateOf(false) }
    var buildingExpanded by remember { mutableStateOf(false) }
    var storeExpanded by remember { mutableStateOf(false) }
    var showImageSourceOptions by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ProductUiState.Created) {
            onCloseClick()
        }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imageBitmap = null
        }
    }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imageBitmap = bitmap
            imageUri = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("Post an Item", style = Typography.titleLarge, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Photos",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9F5D7))
                    .clickable { showImageSourceOptions = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null && imageBitmap == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MutedOlive,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to add a photo",
                            style = Typography.bodyMedium,
                            color = MutedOlive
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageUri ?: imageBitmap,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = "Details",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LightNeutral,
                    focusedBorderColor = Yellow,
                    unfocusedContainerColor = SoftCream,
                    focusedContainerColor = SoftCream
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LightNeutral,
                    focusedBorderColor = Yellow,
                    unfocusedContainerColor = SoftCream,
                    focusedContainerColor = SoftCream
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = LightNeutral,
                        focusedBorderColor = Yellow,
                        unfocusedContainerColor = SoftCream,
                        focusedContainerColor = SoftCream
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Condition", style = Typography.bodyMedium, color = MutedOlive)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conditions.forEach { condition ->
                    val isSelected = selectedCondition == condition
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCondition = condition },
                        label = { Text(condition) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Yellow,
                            selectedLabelColor = Black,
                            containerColor = SoftCream,
                            labelColor = MutedOlive
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = LightNeutral,
                            selectedBorderColor = Yellow,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = buildingExpanded,
                onExpandedChange = { buildingExpanded = !buildingExpanded }
            ) {
                OutlinedTextField(
                    value = buildingLocation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = LightNeutral,
                        focusedBorderColor = Yellow,
                        unfocusedContainerColor = SoftCream,
                        focusedContainerColor = SoftCream
                    )
                )
                ExposedDropdownMenu(
                    expanded = buildingExpanded,
                    onDismissRequest = { buildingExpanded = false }
                ) {
                    buildings.forEach { building ->
                        DropdownMenuItem(
                            text = { Text(building) },
                            onClick = {
                                buildingLocation = building
                                buildingExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                prefix = { Text("$ ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LightNeutral,
                    focusedBorderColor = Yellow,
                    unfocusedContainerColor = SoftCream,
                    focusedContainerColor = SoftCream
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Post as", style = Typography.bodyMedium, color = MutedOlive)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = storeExpanded,
                onExpandedChange = { storeExpanded = !storeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedStore?.name ?: "Personal Profile",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            if (selectedStore == null) Icons.Default.Person else Icons.Default.Store,
                            contentDescription = null
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = LightNeutral,
                        focusedBorderColor = Yellow,
                        unfocusedContainerColor = SoftCream,
                        focusedContainerColor = SoftCream
                    )
                )
                ExposedDropdownMenu(
                    expanded = storeExpanded,
                    onDismissRequest = { storeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Personal Profile (${currentUser?.name ?: "User"})") },
                        onClick = {
                            selectedStore = null
                            storeExpanded = false
                        }
                    )
                    userStores.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name) },
                            onClick = {
                                selectedStore = store
                                storeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is ProductUiState.Error) {
                Text(
                    text = (uiState as ProductUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    productViewModel.createProduct(
                        title = title,
                        description = description,
                        category = selectedCategory,
                        location = buildingLocation,
                        price = price.toDoubleOrNull() ?: 0.0,
                        condition = selectedCondition,
                        storeId = selectedStore?.id,
                        imageUri = imageUri,
                        imageBitmap = imageBitmap
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is ProductUiState.Loading && 
                          title.isNotEmpty() && 
                          price.isNotEmpty() && 
                          (imageUri != null || imageBitmap != null),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Black)
            ) {
                if (uiState is ProductUiState.Loading) {
                    CircularProgressIndicator(color = Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Post Item", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        if (showImageSourceOptions) {
            AlertDialog(
                onDismissRequest = { showImageSourceOptions = false },
                title = { Text("Select Photo Source") },
                text = { Text("Choose how you want to add a photo of your item.") },
                confirmButton = {
                    TextButton(onClick = { 
                        cameraLauncher.launch()
                        showImageSourceOptions = false 
                    }) {
                        Text("Camera", color = MutedOlive)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        galleryLauncher.launch("image/*")
                        showImageSourceOptions = false 
                    }) {
                        Text("Gallery", color = MutedOlive)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostProductScreenPreview() {
    AndesHubTheme {
        PostProductScreen()
    }
}
