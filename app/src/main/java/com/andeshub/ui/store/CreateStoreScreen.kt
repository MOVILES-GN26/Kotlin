package com.andeshub.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.ui.components.InputField
import com.andeshub.ui.theme.*

@Composable
fun CreateStoreScreen(
    onClose: () -> Unit = {}
) {
    var storeName   by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
    ) {
        // Top bar
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
                    tint = Black
                )
            }
            Text(
                text = "Create Store",
                style = MaterialTheme.typography.titleLarge,
                color = Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Logo
        Text(
            text = "Logo",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(LightNeutral)
                .border(1.dp, MutedOlive.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "Agregar logo",
                tint = MutedOlive,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Nombre
        InputField(
            value = storeName,
            onValueChange = { storeName = it },
            placeholder = "Name",
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Descripción
        InputField(
            value = description,
            onValueChange = { description = it },
            placeholder = "Description",
            singleLine = false,
            minLines = 4,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        InputField(
            value = category,
            onValueChange = { category = it },
            placeholder = "Category",
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Aquí irá Category y el botón Create

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = Black
            )
        ) {
            Text(
                text = "Create ",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateStoreScreenPreview() {
    AndesHubTheme {
        CreateStoreScreen()
    }
}