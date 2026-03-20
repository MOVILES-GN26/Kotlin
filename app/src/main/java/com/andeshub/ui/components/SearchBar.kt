package com.andeshub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.R
import com.andeshub.ui.theme.*

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.magnifying_glass),
            contentDescription = "Buscar",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search for items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    AndesHubTheme {
        var text by remember { mutableStateOf("") }
        SearchBar(query = text, onQueryChange = { text = it })
    }
}