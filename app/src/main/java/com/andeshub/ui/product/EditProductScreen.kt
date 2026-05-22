package com.andeshub.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andeshub.data.model.Product
import com.andeshub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    product: Product,
    onBackClick: () -> Unit = {},
    onProductUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val productViewModel: ProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(context) as T
            }
        }
    )

    val editUiState by productViewModel.editUiState.collectAsState()
    val isOnline = remember { mutableStateOf(productViewModel.isNetworkAvailable()) }

    var title by remember { mutableStateOf(product.title) }
    var description by remember { mutableStateOf(product.description) }
    var price by remember { mutableStateOf(product.price.toInt().toString()) }
    var selectedCategory by remember { mutableStateOf(product.category) }
    var selectedCondition by remember { mutableStateOf(product.condition) }
    var selectedLocation by remember { mutableStateOf(product.building_location) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Books & Supplies", "Clothing & Accessories", "Electronics",
        "Food & Drinks", "Furniture", "Sports & Outdoors",
        "Tickets & Events", "Transportation", "Tutoring & Services", "Other"
    )
    val conditions = listOf("New", "Like New", "Good", "Fair")
    val buildings = listOf(
        "Mario Laserna", "Santo Domingo", "Centro del Japón",
        "Biblioteca General", "Cafetería Central"
    )

    LaunchedEffect(editUiState) {
        if (editUiState is ProductUiState.Success) {
            onProductUpdated()
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        if (title.trim().isEmpty()) {
            titleError = "Title is required"
            isValid = false
        } else if (title.trim().length < 3) {
            titleError = "Title must be at least 3 characters"
            isValid = false
        } else if (title.trim().length > 255) {
            titleError = "Title must be less than 255 characters"
            isValid = false
        } else {
            titleError = null
        }

        if (description.trim().isEmpty()) {
            descriptionError = "Description is required"
            isValid = false
        } else if (description.trim().length < 10) {
            descriptionError = "Description must be at least 10 characters"
            isValid = false
        } else if (description.trim().length > 255) {
            descriptionError = "Description must be less than 255 characters"
            isValid = false
        } else {
            descriptionError = null
        }

        val priceValue = price.toDoubleOrNull()
        if (price.isEmpty()) {
            priceError = "Price is required"
            isValid = false
        } else if (priceValue == null) {
            priceError = "Price must be a valid number"
            isValid = false
        } else if (priceValue < 50.0) {
            priceError = "Minimum price is $50 COP"
            isValid = false
        } else if (priceValue > 100_000_000.0) {
            priceError = "Price cannot exceed $100,000,000 COP"
            isValid = false
        } else {
            priceError = null
        }

        return isValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Product",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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

            // Banner offline
            if (!isOnline.value) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No internet connection. Changes cannot be saved until you're back online.",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                "Details",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = { if (titleError != null) Text(titleError!!) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descriptionError = null },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                isError = descriptionError != null,
                supportingText = { if (descriptionError != null) Text(descriptionError!!) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it; priceError = null },
                label = { Text("Price") },
                prefix = { Text("$ ") },
                modifier = Modifier.fillMaxWidth(),
                isError = priceError != null,
                supportingText = { if (priceError != null) Text(priceError!!) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
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

            Text(
                "Condition",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conditions.forEach { condition ->
                    FilterChip(
                        selected = selectedCondition == condition,
                        onClick = { selectedCondition = condition },
                        label = { Text(condition) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = !locationExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    buildings.forEach { building ->
                        DropdownMenuItem(
                            text = { Text(building) },
                            onClick = {
                                selectedLocation = building
                                locationExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (editUiState is ProductUiState.Error) {
                Text(
                    text = (editUiState as ProductUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (validateForm()) {
                        productViewModel.updateProduct(
                            productId = product.id,
                            title = title.trim(),
                            description = description.trim(),
                            category = selectedCategory,
                            location = selectedLocation,
                            price = price,
                            condition = selectedCondition
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = editUiState !is ProductUiState.Loading && isOnline.value,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline.value) MaterialTheme.colorScheme.primary else Color.Gray,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (editUiState is ProductUiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (isOnline.value) "Save Changes" else "No connection to save",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}