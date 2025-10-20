package com.example.mypokedex.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mypokedex.ui.components.CenterText
import com.example.mypokedex.ui.detail.DetailState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailState, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(state.pokemon?.name ?: "Detalle") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
            }
        )
    }) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> CenterText(state.error)
            state.pokemon != null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(model = state.pokemon.imageUrl, contentDescription = state.pokemon.name, modifier = Modifier.size(160.dp))
                Spacer(Modifier.height(12.dp))
                Text("#${state.pokemon.id} — ${state.pokemon.name}", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
