package com.example.mypokedex.ui.exchange

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mypokedex.data.repository.FavoritePokemon
import kotlinx.coroutines.delay

/**
 * Pantalla principal de intercambio
 *
 * Flujo:
 * 1. Usuario selecciona uno de sus favoritos
 * 2. Se genera QR con el exchange ID
 * 3. Otro usuario escanea el QR
 * 4. Se completa el intercambio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    favorites: List<FavoritePokemon>,
    currentUserId: String,
    currentUserAlias: String,
    onBack: () -> Unit,
    onCreateProposal: (FavoritePokemon) -> Unit,
    onScanQR: () -> Unit
) {
    var selectedPokemon by remember { mutableStateOf<FavoritePokemon?>(null) }
    var showQRDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intercambio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onScanQR) {
                        Icon(Icons.Default.QrCode, contentDescription = "Escanear QR")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Selecciona un Pokémon para intercambiar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes Pokémon favoritos",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favorites) { pokemon ->
                        PokemonExchangeCard(
                            pokemon = pokemon,
                            onSelect = {
                                selectedPokemon = pokemon
                                showQRDialog = true
                                onCreateProposal(pokemon)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonExchangeCard(
    pokemon: FavoritePokemon,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "#${pokemon.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Diálogo que muestra el QR para el intercambio
 */
@Composable
fun QRExchangeDialog(
    qrBitmap: Bitmap?,
    exchangeId: String,
    pokemonName: String,
    onDismiss: () -> Unit,
    onTimeout: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(90) }

    LaunchedEffect(exchangeId) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onTimeout()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Compartir Pokémon")
                LinearProgressIndicator(
                    progress = timeLeft / 90f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Compartiendo: $pokemonName",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(256.dp)
                    )
                } else {
                    CircularProgressIndicator()
                }

                Text(
                    "Tiempo restante: ${timeLeft}s",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    "Pide a la otra persona escanear este código",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}