package com.example.mypokedex.ui.features.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import com.example.mypokedex.ui.theme.MypokedexTheme

/**
 * Contenedor que maneja el estado de favoritos de forma elevada.
 * Este es el patrón de state hoisting - el estado vive aquí y se pasa hacia abajo.
 */
@Composable
fun DetailContainer(
    pokemon: Pokemon,
    onBack: () -> Unit = {}
) {
    // Estado elevado - vive en el nivel superior
    val favoritesState = rememberFavoritesState()

    // Verificar si este Pokémon es favorito
    val isFavorite = favoritesState.isFavorite(pokemon.id)

    DetailScreen(
        pokemon = pokemon,
        isFavorite = isFavorite,
        onBack = onBack,
        onToggleFavorite = {
            // La lógica de toggle se maneja en el nivel superior
            favoritesState.toggleFavorite(pokemon.id)
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DetailContainerPreview() {
    MypokedexTheme {
        DetailContainer(
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
            onBack = {}
        )
    }
}