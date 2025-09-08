package com.example.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.ui.components.PokemonCard
import com.example.mypokedex.ui.components.SortFloatingButton
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(ctx))

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var currentSortOrder by rememberSaveable { mutableStateOf(SortOrder.ID_ASCENDING) }

    val pokemonList = viewModel.getPokemonList()

    val processedPokemonList by remember(pokemonList, searchQuery, currentSortOrder) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                pokemonList
            } else {
                pokemonList.filter { pokemon ->
                    pokemon.name.lowercase(Locale.getDefault())
                        .contains(searchQuery.lowercase(Locale.getDefault()))
                }
            }
            sortPokemonList(filtered, currentSortOrder)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val gridState = rememberLazyGridState()

    // Detectar cuando el usuario llega al final de la lista
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    val totalItems = gridState.layoutInfo.totalItemsCount

                    // Cargar más cuando esté cerca del final
                    if (lastVisibleItem.index >= totalItems - 4) {
                        viewModel.loadMorePokemon()
                    }
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            SortFloatingButton(
                currentSortOrder = currentSortOrder,
                onSortClick = { currentSortOrder = currentSortOrder.next() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar Pokémon...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
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

                // Mostrar total de Pokémon cargados
                Card(
                    modifier = Modifier.padding(start = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "${pokemonList.size} Pokémon",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (processedPokemonList.isEmpty() && searchQuery.isNotEmpty()) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(processedPokemonList) { pokemon ->
                        PokemonCard(pokemon = pokemon)
                    }

                    // Mostrar un indicador de carga al final
                    if (processedPokemonList.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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