package com.example.mypokedex.ui.pokedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.ui.components.CenterText
import com.example.mypokedex.ui.components.ErrorBox
import com.example.mypokedex.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    state: PokedexState,
    isConnected: Boolean,
    onSearch: (String) -> Unit,
    onToggleSort: () -> Unit,
    onToggleSortDirection: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokédex") },
                actions = {
                    // Indicador de conexión
                    if (!isConnected) {
                        Icon(
                            imageVector = Icons.Default.SignalWifiOff,
                            contentDescription = "Sin conexión",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Botón de refresh
                    IconButton(onClick = onRefresh, enabled = isConnected) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Banner de estado de conexión
            if (!isConnected) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Sin conexión - Mostrando datos guardados",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Controles de búsqueda y ordenamiento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = state.query,
                    onValueChange = onSearch,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Buscar Pokémon...") }
                )

                // Botón para cambiar tipo de orden (ID/Nombre)
                TextButton(onClick = onToggleSort) {
                    Text(if (state.sortByName) "Nombre" else "ID")
                }

                // Botón para cambiar dirección (ASC/DESC)
                TextButton(onClick = onToggleSortDirection) {
                    Text("↕")
                }
            }

            // Contenido principal
            when {
                state.error != null && state.items.isEmpty() -> {
                    ErrorBox(state.error, onRetry)
                }
                state.isLoading && state.items.isEmpty() -> {
                    LoadingBox("Cargando Pokémon...", Modifier.fillMaxWidth())
                }
                state.items.isEmpty() -> {
                    CenterText(
                        if (state.query.isNotBlank())
                            "No se encontraron Pokémon con '${state.query}'"
                        else
                            "No hay Pokémon disponibles"
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(140.dp),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.items, key = { it.id }) { p ->
                            PokemonCard(p) { onOpen(p.id) }
                        }

                        if (state.isLoading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LoadingBox("Cargando más…")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(p: Pokemon, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = p.imageUrl,
                contentDescription = p.name,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("#${p.id}  ${p.name}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}