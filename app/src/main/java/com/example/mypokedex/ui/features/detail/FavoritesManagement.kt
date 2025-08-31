package com.example.mypokedex.ui.features.detail

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Composable que maneja el estado de los Pokémon favoritos de forma elevada (state hoisting).
 * Utiliza rememberSaveable para persistir los favoritos durante rotaciones y recreaciones.
 */
@Composable
fun rememberFavoritesState(): FavoritesState {
    // Set de IDs de Pokémon favoritos que persiste durante rotaciones
    val favoriteIds by rememberSaveable {
        mutableStateOf(mutableSetOf<Int>())
    }

    return remember {
        FavoritesState(favoriteIds)
    }
}

/**
 * Clase que encapsula el estado y las operaciones de favoritos
 */
class FavoritesState(
    private val favoriteIds: MutableSet<Int>
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
        if (favoriteIds.contains(pokemonId)) {
            favoriteIds.remove(pokemonId)
        } else {
            favoriteIds.add(pokemonId)
        }
    }

    /**
     * Obtiene la lista de IDs de Pokémon favoritos
     */
    fun getFavoriteIds(): Set<Int> {
        return favoriteIds.toSet()
    }
}