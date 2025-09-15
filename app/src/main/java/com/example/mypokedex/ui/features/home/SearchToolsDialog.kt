package com.example.mypokedex.ui.features.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SearchToolsDialog(
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Herramientas de búsqueda") },
        text = { Text("Aquí irán selector y orden (Parte 2). Por ahora solo abrir/cerrar.") },
        confirmButton = { TextButton(onClick = onClose) { Text("Cerrar") } }
    )
}