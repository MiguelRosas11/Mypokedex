package com.example.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.ui.components.PokemonCard
import com.example.mypokedex.ui.components.SortFloatingButton
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = HomeViewModel(),
    modifier: Modifier = Modifier
) {
    // Estado de búsqueda que persiste durante rotaciones y recreaciones
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Estado de ordenamiento que persiste durante rotaciones
    var currentSortOrder by rememberSaveable { mutableStateOf(SortOrder.ID_ASCENDING) }

    // Obtener la lista completa de Pokémon
    val pokemonList = viewModel.getPokemonList()

    // Lista derivada: filtrar y ordenar sin mutar la lista original
    val processedPokemonList by remember(pokemonList, searchQuery, currentSortOrder) {
        derivedStateOf {
            // Primero filtrar
            val filtered = if (searchQuery.isBlank()) {
                pokemonList
            } else {
                pokemonList.filter { pokemon ->
                    pokemon.name.lowercase(Locale.getDefault())
                        .contains(searchQuery.lowercase(Locale.getDefault()))
                }
            }

            // Luego ordenar
            sortPokemonList(filtered, currentSortOrder)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            SortFloatingButton(
                currentSortOrder = currentSortOrder,
                onSortClick = {
                    currentSortOrder = currentSortOrder.next()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text("Buscar Pokémon...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                )
            )

            // Indicador de ordenamiento actual
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Ordenado por: ${currentSortOrder.displayName}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Contenido principal
            if (processedPokemonList.isEmpty() && searchQuery.isNotEmpty()) {
                // Mostrar mensaje cuando no hay resultados
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron Pokémon con \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Grid de Pokémon procesados (filtrados y ordenados)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(processedPokemonList) { pokemon ->
                        PokemonCard(pokemon = pokemon)
                    }
                }
            }
        }
    }
}

/**
 * Función que ordena la lista de Pokémon según el criterio especificado
 */
private fun sortPokemonList(pokemonList: List<Pokemon>, sortOrder: SortOrder): List<Pokemon> {
    return when (sortOrder) {
        SortOrder.ID_ASCENDING -> pokemonList.sortedBy { it.id }
        SortOrder.ID_DESCENDING -> pokemonList.sortedByDescending { it.id }
        SortOrder.NAME_ASCENDING -> pokemonList.sortedBy { it.name.lowercase() }
        SortOrder.NAME_DESCENDING -> pokemonList.sortedByDescending { it.name.lowercase() }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}