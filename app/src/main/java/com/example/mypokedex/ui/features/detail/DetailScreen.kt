package com.example.mypokedex.ui.features.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import com.example.mypokedex.ui.theme.MypokedexTheme

@Composable
fun DetailScreen(
    pokemon: Pokemon,
    isFavorite: Boolean = false,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    LaunchedEffect(isFavorite) {
        println("DEBUG: DetailScreen - Pokemon ${pokemon.name} isFavorite changed to: $isFavorite")
    }

    Scaffold(
        topBar = {
            DetailTopBar(
                pokemonName = pokemon.name,
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = {
                    println("DEBUG: DetailTopBar - Toggle button clicked for ${pokemon.name}")
                    onToggleFavorite()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "#${pokemon.id}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Types
            if (pokemon.types.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pokemon.types.forEach { type ->
                        Card {
                            Text(
                                text = type.name,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            PokemonMeasurements(weight = pokemon.weight, height = pokemon.height)

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            if (pokemon.stats.isNotEmpty()) {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                pokemon.stats.forEach { stat ->
                    PokemonStatRow(stat = stat)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    pokemonName: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TopAppBar(
        title = { Text(pokemonName) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos"
                )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DetailScreenPreview() {
    MypokedexTheme {
        DetailScreen(
            pokemon = Pokemon(
                id = 25,
                name = "Pikachu",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
                types = listOf(PokemonType.ELECTRIC),
                height = 0.4f,
                weight = 6.0f,
                stats = listOf(
                    PokemonStat("HP", 35),
                    PokemonStat("Attack", 55),
                    PokemonStat("Defense", 40)
                )
            ),
            isFavorite = true,
            onBack = {},
            onToggleFavorite = {}
        )
    }
}