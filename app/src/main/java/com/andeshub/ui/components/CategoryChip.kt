package com.andeshub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andeshub.ui.theme.*

data class Category(
    val label: String,
    val icon: ImageVector
)

@Composable
fun CategoryChip(
    category: Category,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = LightNeutral
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.label,
                tint = Black,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyMedium,
                color = Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipPreview() {
    AndesHubTheme {
        CategoryChip(
            category = Category("Books", Icons.Outlined.MenuBook)
        )
    }
}

