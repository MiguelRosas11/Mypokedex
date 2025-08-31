package com.example.mypokedex.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mypokedex.ui.features.home.SortOrder

/**
 * Versión ultra simple del botón flotante usando ícono básico
 */
@Composable
fun SortFloatingButton(
    currentSortOrder: SortOrder,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onSortClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.List, // Ícono de lista - siempre disponible
            contentDescription = "Ordenar por ${currentSortOrder.displayName}"
        )
    }
}