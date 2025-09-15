package com.example.mypokedex.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SortBy { Numero, Nombre }

@Composable
fun SearchToolsScreen(
    onApply: (SortBy, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(SortBy.Numero) }
    var ascending by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Herramientas de Búsqueda", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ordenar por", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = if (sortBy == SortBy.Numero) "Número" else "Nombre",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                enabled = false
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Número") }, onClick = { sortBy = SortBy.Numero; expanded = false })
                DropdownMenuItem(text = { Text("Nombre") }, onClick = { sortBy = SortBy.Nombre; expanded = false })
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Orden", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row {
                    RadioButton(selected = ascending, onClick = { ascending = true })
                    Spacer(Modifier.width(8.dp)); Text("Ascendente")
                }
                Row {
                    RadioButton(selected = !ascending, onClick = { ascending = false })
                    Spacer(Modifier.width(8.dp)); Text("Descendente")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { onApply(sortBy, ascending) }) { Text("Aplicar") }
        }
    }
}