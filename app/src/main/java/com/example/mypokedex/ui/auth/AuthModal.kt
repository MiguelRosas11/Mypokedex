package com.example.mypokedex.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Modal de autenticación rápida
 * Diseñado para ser lo más rápido posible por la misma compe
 */
@Composable
fun AuthModal(
    onDismiss: () -> Unit,
    onAuthSuccess: (String) -> Unit,
    onAuthError: (String) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Inicio Rápido",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    "Ingresa un alias para comenzar",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Campo de alias
                OutlinedTextField(
                    value = alias,
                    onValueChange = {
                        if (it.length <= 8) alias = it.uppercase()
                    },
                    label = { Text("Alias (3-8 caracteres)") },
                    placeholder = { Text("PIKACHU") },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (alias.length in 3..8 && !isLoading) {
                                isLoading = true
                                onAuthSuccess(alias)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Contador de caracteres
                Text(
                    "${alias.length}/8",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alias.length in 3..8)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                // Botón de inicio
                Button(
                    onClick = {
                        isLoading = true
                        onAuthSuccess(alias)
                    },
                    enabled = alias.length in 3..8 && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("INICIAR")
                    }
                }

                // Ayuda
                Text(
                    " ;D Login anónimo ultra-rápido",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}