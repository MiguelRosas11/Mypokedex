package com.example.mypokedex.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mypokedex.ui.features.home.SortOrder

/**
 * Botón flotante para ordenar la lista de Pokémon.
 * Implementa state hoisting - no maneja su propio estado de ordenamiento.
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
            imageVector = getSortIcon(currentSortOrder),
            contentDescription = "Ordenar por ${currentSortOrder.displayName}"
        )
    }
}

/**
 * Función helper para obtener el ícono apropiado según el tipo de ordenamiento
 */
private fun getSortIcon(sortOrder: SortOrder): ImageVector {
    // Por ahora usamos el mismo ícono para todos, pero se puede personalizar
    return Icons.Default.Sort
}