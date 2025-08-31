package com.example.mypokedex.ui.features.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.mypokedex.ui.components.FavoriteButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    pokemonName: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = pokemonName.replaceFirstChar { it.uppercase() }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            FavoriteButton(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }
    )
}