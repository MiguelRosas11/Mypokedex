package com.example.mypokedex.ui.features.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    pokemonName: String,
    onBack: () -> Unit,
    onFavorite: () -> Unit
) {
    TopAppBar(
        title = { Text(text = pokemonName) },
        navigationIcon = {
            TextButton(onClick = onBack) { Text("←") }   // flecha simple, sin íconos
        },
        actions = {
            TextButton(onClick = onFavorite) { Text("♡") } // corazón simple, sin íconos
        }
    )
}