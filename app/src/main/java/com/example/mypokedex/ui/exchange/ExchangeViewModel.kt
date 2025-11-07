package com.example.mypokedex.ui.exchange

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.AuthRepository
import com.example.mypokedex.data.repository.ExchangeProposal
import com.example.mypokedex.data.repository.ExchangeRepository
import com.example.mypokedex.data.repository.FavoritePokemon
import com.example.mypokedex.data.repository.FavoritesRepository
import com.example.mypokedex.data.repository.ExchangeStatus
import com.example.mypokedex.util.QRCodeGenerator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExchangeEvent {
    data class ExchangeCompleted(val message: String) : ExchangeEvent()
    data class ExchangeFailed(val error: String) : ExchangeEvent()
}

data class ExchangeState(
    val userFavorites: List<FavoritePokemon> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPokemon: FavoritePokemon? = null,
    val qrCodeBitmap: Bitmap? = null,
    val currentExchangeId: String? = null,
    val proposalToAccept: ExchangeProposal? = null,
    val exchangeError: String? = null
)

class ExchangeViewModel(
    private val authRepo: AuthRepository,
    private val favoritesRepo: FavoritesRepository,
    private val exchangeRepo: ExchangeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExchangeState())
    val state: StateFlow<ExchangeState> = _state.asStateFlow()

    private val _event = Channel<ExchangeEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadUserFavorites()
    }

    private fun loadUserFavorites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = authRepo.getCurrentUserId()
            if (userId != null) {
                // !! CORREGIDO: Usar el nombre de función correcto
                favoritesRepo.getUserFavorites(userId)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                exchangeError = e.message ?: "Error al cargar favoritos"
                            )
                        }
                    }
                    .collect { favorites ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                userFavorites = favorites
                            )
                        }
                    }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        exchangeError = "Usuario no autenticado"
                    )
                }
            }
        }
    }

    fun createExchangeProposal(pokemon: FavoritePokemon, userAlias: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, selectedPokemon = pokemon) }
            val userId = authRepo.getCurrentUserId() ?: return@launch

            val result = exchangeRepo.createExchangeProposal(
                userAId = userId,
                userAAlias = userAlias,
                pokemonAId = pokemon.id,
                pokemonAName = pokemon.name,
                pokemonAImageUrl = pokemon.imageUrl
            )

            result.fold(
                onSuccess = { exchangeId ->
                    // !! CORREGIDO: Usar el nombre de función correcto
                    val bitmap = QRCodeGenerator.generateQRCode(exchangeId, 300, 300)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            qrCodeBitmap = bitmap,
                            currentExchangeId = exchangeId
                        )
                    }
                    listenForExchangeUpdates(exchangeId)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            exchangeError = error.message ?: "Error al crear propuesta"
                        )
                    }
                }
            )
        }
    }

    fun loadExchangeProposal(exchangeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val proposal = exchangeRepo.getExchangeProposal(exchangeId)
            if (proposal != null) {
                if (proposal.status == ExchangeStatus.EXPIRED || proposal.status == ExchangeStatus.CANCELLED) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            exchangeError = "Este intercambio ya ha expirado o fue cancelado."
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            proposalToAccept = proposal
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        exchangeError = "Propuesta de intercambio no encontrada o inválida."
                    )
                }
            }
        }
    }

    fun acceptExchange(exchangeId: String, userBAlias: String, pokemon: FavoritePokemon) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = authRepo.getCurrentUserId() ?: return@launch

            runCatching {
                exchangeRepo.acceptExchange(
                    exchangeId = exchangeId,
                    userBId = userId,
                    userBAlias = userBAlias,
                    pokemonBId = pokemon.id,
                    pokemonBName = pokemon.name,
                    pokemonBImageUrl = pokemon.imageUrl
                )
            }.fold(
                onSuccess = { result ->
                    result.fold(
                        onSuccess = {
                            _state.update { it.copy(isLoading = false) }
                            _event.send(ExchangeEvent.ExchangeCompleted("¡Intercambio exitoso!"))
                        },
                        onFailure = { error ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    exchangeError = error.message ?: "Error al aceptar el intercambio"
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            exchangeError = error.message ?: "Error al aceptar el intercambio"
                        )
                    }
                }
            )
        }
    }

    private fun listenForExchangeUpdates(exchangeId: String) {
        viewModelScope.launch {
            exchangeRepo.observeExchangeProposal(exchangeId).collect { proposal ->
                when (proposal?.status) {
                    ExchangeStatus.COMPLETED -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                qrCodeBitmap = null,
                                currentExchangeId = null
                            )
                        }
                        _event.send(ExchangeEvent.ExchangeCompleted("¡Intercambio completado!"))
                    }
                    ExchangeStatus.CANCELLED -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                qrCodeBitmap = null,
                                currentExchangeId = null
                            )
                        }
                        _event.send(ExchangeEvent.ExchangeFailed("El intercambio fue cancelado."))
                    }
                    ExchangeStatus.EXPIRED -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                qrCodeBitmap = null,
                                currentExchangeId = null
                            )
                        }
                        _event.send(ExchangeEvent.ExchangeFailed("El tiempo ha expirado."))
                    }
                    else -> { /* Sigue en PENDING */ }
                }
            }
        }
    }

    fun cancelExchange() {
        viewModelScope.launch {
            val exchangeId = _state.value.currentExchangeId ?: return@launch
            exchangeRepo.cancelExchange(exchangeId)
            _state.update {
                it.copy(
                    qrCodeBitmap = null,
                    currentExchangeId = null,
                    selectedPokemon = null
                )
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(exchangeError = null) }
    }
}