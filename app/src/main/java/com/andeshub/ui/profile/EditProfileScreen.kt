package com.andeshub.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.andeshub.data.ALLOWED_MAJORS
import com.andeshub.ui.components.InputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var majorExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateAvatar(it) }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaveSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.avatarUrl!!
                            .replace("http://localhost:9000", "http://192.168.1.76:9000"),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = uiState.firstName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap to change photo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputField(
                value = uiState.firstName,
                onValueChange = { viewModel.onFirstNameChange(it) },
                label = "First_Name",
                placeholder = "Enter your first name"
            )
            InputField(
                value = uiState.lastName,
                onValueChange = { viewModel.onLastNameChange(it) },
                label = "Last_Name",
                placeholder = "Enter your last name"
            )


            ExposedDropdownMenuBox(
                expanded = majorExpanded,
                onExpandedChange = { majorExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.major,
                    onValueChange = { viewModel.onMajorChange(it) },
                    label = { Text("Major") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    )
                )
                val filtered = ALLOWED_MAJORS.filter {
                    it.contains(uiState.major, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = majorExpanded,
                        onDismissRequest = { majorExpanded = false }
                    ) {
                        filtered.forEach { major ->
                            DropdownMenuItem(
                                text = { Text(major) },
                                onClick = {
                                    viewModel.onMajorChange(major)
                                    majorExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+57",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    InputField(
                        value = uiState.phoneNumber,
                        onValueChange = { viewModel.onPhoneNumberChange(it) },
                        placeholder = "3001234567",
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            // Password
            InputField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "New Password (optional)",
                placeholder = "Enter new password",
                isPassword = true
            )
            InputField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordError = null
                },
                label = "Confirm Password",
                placeholder = "Confirm new password",
                isPassword = true
            )

            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón guardar
        Button(
            onClick = {
                if (password.isNotBlank() && password != confirmPassword) {
                    passwordError = "Las contraseñas no coinciden"
                    return@Button
                }
                viewModel.saveProfile(password.takeIf { it.isNotBlank() })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}