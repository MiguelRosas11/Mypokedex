package com.example.mypokedex.ui.exchange

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mypokedex.data.repository.FavoritePokemon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptExchangeScreen(
    proposalPokemon: FavoritePokemon?,
    userFavorites: List<FavoritePokemon>,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onAccept: (FavoritePokemon) -> Unit,
    onErrorDismiss: () -> Unit
) {
    var selectedPokemon by remember { mutableStateOf<FavoritePokemon?>(null) }

    if (error != null) {
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            title = { Text("Error en el Intercambio") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aceptar Intercambio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (proposalPokemon != null && selectedPokemon != null && !isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { onAccept(selectedPokemon!!) },
                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = "Intercambiar") },
                    text = { Text("Confirmar Intercambio") },
                    expanded = true
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // !! CORREGIDO: Usar CircularProgressIndicator
            if (isLoading && proposalPokemon == null) {
                CircularProgressIndicator()
            }

            if (proposalPokemon != null) {
                Text("Te ofrecen:", style = MaterialTheme.typography.titleMedium)
                PokemonCard(pokemon = proposalPokemon, isSelected = false, onClick = {})
                Spacer(modifier = Modifier.height(24.dp))

                Text("Elige tu Pokémon a intercambiar:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userFavorites) { pokemon ->
                        PokemonCard(
                            pokemon = pokemon,
                            isSelected = selectedPokemon?.id == pokemon.id,
                            onClick = { if (!isLoading) selectedPokemon = pokemon }
                        )
                    }
                }
            } else if (!isLoading) {
                Text("Propuesta no válida o expirada.")
            }

            // Overlay de carga
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    // !! CORREGIDO: Usar CircularProgressIndicator
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(
    pokemon: FavoritePokemon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = pokemon.imageUrl),
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.small),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = pokemon.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}