package com.example.mypokedex.ui.pokedex

import com.example.mypokedex.data.model.Pokemon

data class PokedexState(
    val items: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canLoadMore: Boolean = true,
    val query: String = "",
    val sortByName: Boolean = false
)
