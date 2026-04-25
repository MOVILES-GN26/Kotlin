package com.andeshub.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andeshub.ui.components.InputField
import com.andeshub.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoreScreen(
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: StoreViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoreViewModel(context) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Carga el borrador al abrir la pantalla
    val draft = remember { viewModel.loadDraft() }

    var storeName        by remember { mutableStateOf(draft?.first ?: "") }
    var description      by remember { mutableStateOf(draft?.second ?: "") }
    var category         by remember { mutableStateOf(draft?.third ?: "") }
    var storeNameError   by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var expanded         by remember { mutableStateOf(false) }
    var logoUri          by remember { mutableStateOf<Uri?>(null) }

    val categories = listOf("Clothing", "Accessories", "Food", "Technology", "Home", "Entertainment")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        logoUri = uri
    }

    LaunchedEffect(uiState) {
        if (uiState is StoreUiState.Success) {
            viewModel.clearDraft() // borra el borrador al crear exitosamente
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Create Store",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Aviso de falta de conexión si hay error de tipo isOffline
        if (uiState is StoreUiState.Error && (uiState as StoreUiState.Error).isOffline) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = (uiState as StoreUiState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Aviso de borrador si hay datos guardados
        if (draft != null && (draft.first.isNotEmpty() || draft.second.isNotEmpty() || draft.third.isNotEmpty())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Draft restored",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable {
                        viewModel.clearDraft()
                        storeName = ""
                        description = ""
                        category = ""
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Logo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), CircleShape)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (logoUri != null) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AddAPhoto,
                    contentDescription = "Agregar logo",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        InputField(
            value = storeName,
            onValueChange = {
                storeName = it
                storeNameError = if (it.length > 120) "Maximum 120 characters" else ""
                viewModel.saveDraft(it, description, category)
            },
            placeholder = "Name",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        if (storeNameError.isNotEmpty()) {
            Text(
                text = storeNameError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InputField(
            value = description,
            onValueChange = {
                description = it
                descriptionError = if (it.length > 120) "Maximum 120 characters" else ""
                viewModel.saveDraft(storeName, it, category)
            },
            placeholder = "Description",
            singleLine = false,
            minLines = 4,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        if (descriptionError.isNotEmpty()) {
            Text(
                text = descriptionError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            category = option
                            expanded = false
                            viewModel.saveDraft(storeName, description, option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState is StoreUiState.Error && !(uiState as StoreUiState.Error).isOffline) {
            Text(
                text = (uiState as StoreUiState.Error).message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        Button(
            onClick = {
                viewModel.createStore(
                    name = storeName,
                    description = description,
                    category = category,
                    logoUri = logoUri
                )
            },
            enabled = storeName.isNotEmpty() &&
                    description.isNotEmpty() &&
                    category.isNotEmpty() &&
                    uiState !is StoreUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        ) {
            if (uiState is StoreUiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Create",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun CreateStoreScreenPreview() {
    AndesHubTheme {
        CreateStoreScreen()
    }
}