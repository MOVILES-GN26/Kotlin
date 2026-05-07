package com.andeshub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andeshub.data.ConnectivityObserver

@Composable
fun ConnectivityStatusView(status: ConnectivityObserver.Status) {
    val isVisible = status != ConnectivityObserver.Status.Available
    
    val backgroundColor by animateColorAsState(
        targetValue = if (status == ConnectivityObserver.Status.Losing) {
            Color(0xFFFFA000) 
        } else {
            Color(0xFFD32F2F) 
        },
        label = "backgroundColor"
    )

    val message = when (status) {
        ConnectivityObserver.Status.Losing -> "unstable connection..."
        ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> "Offline"
        else -> ""
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .statusBarsPadding() // Asegura que el contenido esté debajo de la barra de estado, pero el fondo la cubra
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
