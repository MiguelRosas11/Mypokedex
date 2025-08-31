package com.example.mypokedex.ui.features.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    isFavorite: Boolean,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            DetailTopBar(
                pokemonName = pokemon.name,
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            PokemonMeasurements(weight = pokemon.weight, height = pokemon.height)

            pokemon.stats.forEach { stat ->
                PokemonStatRow(stat = stat)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DetailScreenPreview() {
    MypokedexTheme {
        DetailScreen(
            pokemon = Pokemon(
                id = 25,
                name = "Pikachu",
                types = listOf(PokemonType.ELECTRIC),
                height = 0.4f,
                weight = 6.0f,
                stats = listOf(
                    PokemonStat("HP", 35),
                    PokemonStat("Attack", 55),
                    PokemonStat("Defense", 40)
                )
            ),
            isFavorite = true, // Para el preview
            onBack = {},
            onToggleFavorite = {}
        )
    }
}