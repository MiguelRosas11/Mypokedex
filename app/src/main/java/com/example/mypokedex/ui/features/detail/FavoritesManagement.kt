package com.example.mypokedex.ui.features.detail

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Composable que maneja el estado de los Pokémon favoritos de forma elevada (state hoisting).
 * Utiliza rememberSaveable para persistir los favoritos durante rotaciones y recreaciones.
 */
@Composable
fun rememberFavoritesState(): FavoritesState {
    // Set de IDs de Pokes favoritos que persiste durante rotaciones
    var favoriteIds by rememberSaveable {
        mutableStateOf(setOf<Int>())
    }

    return remember(favoriteIds) {
        FavoritesState(
            favoriteIds = favoriteIds,
            onFavoritesChanged = { newFavorites ->
                favoriteIds = newFavorites
            }
        )
    }
}

/**
 * Clase que encapsula el estado y las operaciones de favoritos
 */
class FavoritesState(
    private val favoriteIds: Set<Int>,
    private val onFavoritesChanged: (Set<Int>) -> Unit
) {
    /**
     * Verifica si un Pokémon es favorito
     */
    fun isFavorite(pokemonId: Int): Boolean {
        return favoriteIds.contains(pokemonId)
    }

    /**
     * Alterna el estado de favorito de un Pokémon
     */
    fun toggleFavorite(pokemonId: Int) {
        val newFavorites = if (favoriteIds.contains(pokemonId)) {
            favoriteIds - pokemonId
        } else {
            favoriteIds + pokemonId
        }
        onFavoritesChanged(newFavorites)

        // Debug: Verificar que funciona
        println("DEBUG: Pokemon $pokemonId toggled. Favorites: $newFavorites")
    }

    /**
     * Obtiene la lista de IDs de Pokémon favoritos
     */
    fun getFavoriteIds(): Set<Int> {
        return favoriteIds
    }
}