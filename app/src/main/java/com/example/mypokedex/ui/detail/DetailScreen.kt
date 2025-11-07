package com.example.mypokedex.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    isFavorite: Boolean = false,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.pokemon?.name ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state.pokemon != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite)
                                    Icons.Default.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite)
                                    "Quitar de favoritos"
                                else
                                    "Agregar a favoritos",
                                tint = if (isFavorite)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error)
                }
            }
            state.pokemon != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = state.pokemon.imageUrl,
                        contentDescription = state.pokemon.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "#${state.pokemon.id.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = state.pokemon.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tipos
                    if (state.pokemon.types.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.pokemon.types.forEach { type ->
                                Card {
                                    Text(
                                        text = type.name,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Medidas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Peso", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${state.pokemon.weight} kg",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Altura", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${state.pokemon.height} m",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estadísticas
                    if (state.pokemon.stats.isNotEmpty()) {
                        Text(
                            "Estadísticas",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        state.pokemon.stats.forEach { stat ->
                            PokemonStatRow(stat = stat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonStatRow(stat: PokemonStat) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stat.name.replaceFirstChar { it.uppercase() },
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = (stat.value.coerceIn(0, 255)) / 255f,
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        )
        Text(
            text = stat.value.toString(),
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}