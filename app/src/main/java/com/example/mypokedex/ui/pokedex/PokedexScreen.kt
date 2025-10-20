package com.example.mypokedex.ui.pokedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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

@Composable
fun PokedexScreen(
    state: PokedexState,
    onSearch: (String) -> Unit,
    onToggleSort: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = state.query,
                onValueChange = onSearch,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Buscar…") }
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onToggleSort) {
                Text(if (state.sortByName) "Nombre" else "#")
            }
        }

        when {
            state.error != null -> ErrorBox(state.error, onRetry)
            state.items.isEmpty() && state.isLoading -> LoadingBox("Cargando..", Modifier.fillMaxWidth())
            state.items.isEmpty() -> CenterText("Sin resultados")
            else -> {
                val listState = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    state = listState,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items) { p ->
                        PokemonCard(p) { onOpen(p.id) }
                    }
                    if (state.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingBox("Cargando más…")
                        }
                    }
                }

                val shouldLoadMore by remember {
                    derivedStateOf {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val total = listState.layoutInfo.totalItemsCount
                        state.canLoadMore && last >= total - 8
                    }
                }
                LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) onLoadMore() }
            }
        }
    }
}

@Composable
private fun PokemonCard(p: Pokemon, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(model = p.imageUrl, contentDescription = p.name, modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(8.dp))
            Text("#${p.id}  ${p.name}")
        }
    }
}
