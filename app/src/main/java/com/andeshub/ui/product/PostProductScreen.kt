package com.andeshub.ui.product

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.andeshub.data.local.ProductDraftEntity
import com.andeshub.data.model.Store
import com.andeshub.data.model.UserProfile
import com.andeshub.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProductScreen(
    currentUser: UserProfile? = null,
    initialDraft: ProductDraftEntity? = null,
    onCloseClick: () -> Unit = {},
    onViewDraftsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val productViewModel: ProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(context) as T
            }
        }
    )

    val uiState by productViewModel.uiState.collectAsState()
    val userStores by productViewModel.userStores.collectAsState()
    val topCategories by productViewModel.topCategories.collectAsState()
    
    val isOnline = remember { mutableStateOf(productViewModel.isNetworkAvailable()) }
    
    LaunchedEffect(Unit) {
        productViewModel.loadTopCategoriesThisWeek()
        while (true) {
            isOnline.value = productViewModel.isNetworkAvailable()
            delay(2000)
        }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var buildingLocation by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedStore by remember { mutableStateOf<Store?>(null) }
    
    val draftRestored = remember { mutableStateOf(false) }

    // Validation States
    var titleError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var conditionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

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

    // Función para limpiar todo el formulario y el borrador temporal
    fun clearForm() {
        title = ""
        description = ""
        price = ""
        selectedCategory = ""
        selectedCondition = ""
        buildingLocation = ""
        imageUri = null
        imageBitmap = null
        selectedStore = null
        productViewModel.clearDraft()
        draftRestored.value = false
    }

    // 1. Cargar borrador persistente de Room (cuando vienes de la lista)
    LaunchedEffect(initialDraft) {
        initialDraft?.let {
            title = it.title
            description = it.description
            price = it.price
            selectedCategory = it.category
            selectedCondition = it.condition
            buildingLocation = it.location
            imageUri = it.imageUri?.let { uriStr -> Uri.parse(uriStr) }
            draftRestored.value = true
        }
    }

    // 2. Cargar borrador automático de SharedPreferences (el de "antes")
    LaunchedEffect(Unit) {
        if (initialDraft == null) {
            val draft = productViewModel.loadDraft()
            if (draft != null && (draft.first.isNotEmpty() || draft.second.isNotEmpty())) {
                title = draft.first
                description = draft.second
                draftRestored.value = true
            }
        }
    }

    // Auto-guardado del borrador temporal mientras escribes
    LaunchedEffect(title, description) {
        if (title.isNotEmpty() || description.isNotEmpty()) {
            productViewModel.saveDraft(title, description)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ProductUiState.Created) {
            onCloseClick()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imageBitmap = null
            imageError = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imageBitmap = bitmap
            imageUri = null
            imageError = null
        }
    }

    fun validateForm(): Boolean {
        var isValid = true
        if (title.trim().isEmpty()) { titleError = "Title is required"; isValid = false } else { titleError = null }
        val priceValue = price.toDoubleOrNull()
        if (price.isEmpty()) { priceError = "Price is required"; isValid = false } 
        else if (priceValue == null) { priceError = "Price must be a valid number"; isValid = false }
        else if (priceValue < 50.0) { priceError = "Minimum price is $50 COP"; isValid = false }
        else { priceError = null }
        if (description.trim().isEmpty()) { descriptionError = "Description is required"; isValid = false } 
        else if (description.trim().length < 10) { descriptionError = "Description must be at least 10 characters"; isValid = false }
        else { descriptionError = null }
        if (selectedCategory.isEmpty()) { categoryError = "Category is required"; isValid = false } else { categoryError = null }
        if (selectedCondition.isEmpty()) { conditionError = "Condition is required"; isValid = false } else { conditionError = null }
        if (buildingLocation.isEmpty()) { locationError = "Location is required"; isValid = false } else { locationError = null }
        if (imageUri == null && imageBitmap == null) { imageError = "At least one photo is required"; isValid = false } else { imageError = null }
        return isValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Post an Item", style = Typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onViewDraftsClick) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "My Drafts", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            // Warning de conexión
            AnimatedVisibility(
                visible = !isOnline.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                        Text(text = "An internet connection is required to post a product. Please check your connection.", style = Typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            if (draftRestored.value) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = if (initialDraft != null) "Draft loaded from list" else "Previous draft restored", style = Typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Row {
                            Text(text = "View drafts", style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onViewDraftsClick() })
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Clear", style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error, modifier = Modifier.clickable {
                                clearForm()
                            })
                        }
                    }
                }
            }

            // Banner de categorías trending
            if (topCategories.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(text = "Top categories this week", style = Typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        topCategories.forEachIndexed { index, item ->
                            Text(text = "${index + 1}. ${item.category} — ${item.purchases} purchases", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            Text(text = "Photos", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(vertical = 12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).border(width = 1.dp, color = if (imageError != null) MaterialTheme.colorScheme.error else Color.Transparent, shape = RoundedCornerShape(16.dp)).clickable { showImageSourceOptions = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null && imageBitmap == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = if (imageError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Tap to add a photo", style = Typography.bodyMedium, color = if (imageError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    AsyncImage(model = imageUri ?: imageBitmap, contentDescription = "Selected image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            if (imageError != null) {
                Text(text = imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }

            Text(text = "Details", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))

            OutlinedTextField(
                value = title, onValueChange = { title = it; if (titleError != null) validateForm() }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), isError = titleError != null,
                supportingText = { if (titleError != null) Text(titleError!!) }, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description, onValueChange = { description = it; if (descriptionError != null) validateForm() }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp), isError = descriptionError != null,
                supportingText = { if (descriptionError != null) Text(descriptionError!!) }, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = selectedCategory, onValueChange = {}, readOnly = true, label = { Text("Category") }, isError = categoryError != null,
                    supportingText = { if (categoryError != null) Text(categoryError!!) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { category -> DropdownMenuItem(text = { Text(category) }, onClick = { selectedCategory = category; categoryExpanded = false; categoryError = null }) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Condition", style = Typography.bodyMedium, color = if (conditionError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                conditions.forEach { condition ->
                    val isSelected = selectedCondition == condition
                    FilterChip(
                        selected = isSelected, onClick = { selectedCondition = condition; conditionError = null }, label = { Text(condition) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, containerColor = MaterialTheme.colorScheme.surface, labelColor = MaterialTheme.colorScheme.secondary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (conditionError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface, selectedBorderColor = MaterialTheme.colorScheme.primary, borderWidth = 1.dp),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            if (conditionError != null) { Text(text = conditionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = buildingExpanded, onExpandedChange = { buildingExpanded = !buildingExpanded }) {
                OutlinedTextField(
                    value = buildingLocation, onValueChange = {}, readOnly = true, label = { Text("Building Location") }, isError = locationError != null,
                    supportingText = { if (locationError != null) Text(locationError!!) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
                )
                ExposedDropdownMenu(expanded = buildingExpanded, onDismissRequest = { buildingExpanded = false }) {
                    buildings.forEach { building -> DropdownMenuItem(text = { Text(building) }, onClick = { buildingLocation = building; buildingExpanded = false; locationError = null }) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price, onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || (it.endsWith(".") && it.count { c -> c == '.' } <= 1)) { price = it; if (priceError != null) validateForm() } },
                label = { Text("Price") }, prefix = { Text("$ ") }, modifier = Modifier.fillMaxWidth(), isError = priceError != null, supportingText = { if (priceError != null) Text(priceError!!) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Post as", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = storeExpanded, onExpandedChange = { storeExpanded = !storeExpanded }) {
                OutlinedTextField(
                    value = selectedStore?.name ?: "Personal Profile", onValueChange = {}, readOnly = true, leadingIcon = { Icon(if (selectedStore == null) Icons.Default.Person else Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surface, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
                )
                ExposedDropdownMenu(expanded = storeExpanded, onDismissRequest = { storeExpanded = false }) {
                    DropdownMenuItem(text = { Text("Personal Profile (${currentUser?.name ?: "User"})") }, onClick = { selectedStore = null; storeExpanded = false })
                    userStores.forEach { store -> DropdownMenuItem(text = { Text(store.name) }, onClick = { selectedStore = store; storeExpanded = false }) }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is ProductUiState.Error) {
                Text(text = (uiState as ProductUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            // BOTONES DE ACCIÓN
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        productViewModel.saveProductAsDraft(title, description, selectedCategory, buildingLocation, price, selectedCondition, imageUri?.toString(), selectedStore?.id)
                        clearForm() // Limpiar borrador temporal al guardar uno permanente
                        onViewDraftsClick()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Draft")
                }

                Button(
                    onClick = {
                        if (!productViewModel.isNetworkAvailable()) { isOnline.value = false; return@Button }
                        if (validateForm()) {
                            productViewModel.createProduct(title.trim(), description.trim(), selectedCategory, buildingLocation, price.toDoubleOrNull() ?: 0.0, selectedCondition, selectedStore?.id, imageUri, imageBitmap)
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = uiState !is ProductUiState.Loading && isOnline.value,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is ProductUiState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = if (isOnline.value) "Post Item" else "Offline", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        if (showImageSourceOptions) {
            AlertDialog(
                onDismissRequest = { showImageSourceOptions = false },
                title = { Text("Select Photo Source") },
                text = { Text("Choose how you want to add a photo of your item.") },
                confirmButton = { TextButton(onClick = { cameraLauncher.launch(); showImageSourceOptions = false }) { Text("Camera", color = MaterialTheme.colorScheme.secondary) } },
                dismissButton = { TextButton(onClick = { galleryLauncher.launch("image/*"); showImageSourceOptions = false }) { Text("Gallery", color = MaterialTheme.colorScheme.secondary) } }
            )
        }
    }
}
