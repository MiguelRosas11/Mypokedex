package com.example.mypokedex.ui.detail

import com.example.mypokedex.data.model.Pokemon

data class DetailState(
    val pokemon: Pokemon? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
