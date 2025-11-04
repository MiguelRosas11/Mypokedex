package com.example.mypokedex.ui.exchange

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.*
import com.example.mypokedex.util.QRCodeGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExchangeViewModel(
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository,
    private val exchangeRepository: ExchangeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExchangeState())
    val state: StateFlow<ExchangeState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<ExchangeEvent>()
    val event: SharedFlow<ExchangeEvent> = _event.asSharedFlow()

    init {
        loadUserFavorites()
    }

    /**
     * Cargar favoritos del usuario actual
     */
    private fun loadUserFavorites() {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            favoritesRepository.getUserFavorites(userId)
                .catch { e ->
                    _state.update { it.copy(error = e.localizedMessage) }
                }
                .collect { favorites ->
                    _state.update { it.copy(userFavorites = favorites) }
                }
        }
    }

    /**
     * Crear propuesta de intercambio
     */
    fun createExchangeProposal(pokemon: FavoritePokemon, userAlias: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = exchangeRepository.createExchangeProposal(
                userAId = userId,
                userAAlias = userAlias,
                pokemonAId = pokemon.id,
                pokemonAName = pokemon.name,
                pokemonAImageUrl = pokemon.imageUrl
            )

            result.fold(
                onSuccess = { exchangeId ->
                    // Generar QR Code
                    val qrBitmap = QRCodeGenerator.generateQRCode(exchangeId)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentExchangeId = exchangeId,
                            qrCodeBitmap = qrBitmap,
                            selectedPokemon = pokemon
                        )
                    }

                    // Observar cambios en el intercambio
                    observeExchange(exchangeId)
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage
                        )
                    }
                }
            )
        }
    }

    /**
     * Observar cambios en un intercambio
     */
    private fun observeExchange(exchangeId: String) {
        viewModelScope.launch {
            exchangeRepository.observeExchangeProposal(exchangeId)
                .collect { proposal ->
                    if (proposal?.status == ExchangeStatus.COMPLETED) {
                        _event.emit(ExchangeEvent.ExchangeCompleted)
                        _state.update {
                            it.copy(
                                currentExchangeId = null,
                                qrCodeBitmap = null,
                                selectedPokemon = null
                            )
                        }
                    }
                }
        }
    }

    /**
     * Aceptar propuesta de intercambio
     */
    fun acceptExchange(
        exchangeId: String,
        userAlias: String,
        selectedPokemon: FavoritePokemon
    ) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = exchangeRepository.acceptExchange(
                exchangeId = exchangeId,
                userBId = userId,
                userBAlias = userAlias,
                pokemonBId = selectedPokemon.id,
                pokemonBName = selectedPokemon.name,
                pokemonBImageUrl = selectedPokemon.imageUrl
            )

            result.fold(
                onSuccess = {
                    _event.emit(ExchangeEvent.ExchangeCompleted)
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage
                        )
                    }
                    _event.emit(ExchangeEvent.ExchangeFailed(e.localizedMessage ?: "Error"))
                }
            )
        }
    }

    /**
     * Cancelar intercambio
     */
    fun cancelExchange() {
        val exchangeId = _state.value.currentExchangeId ?: return

        viewModelScope.launch {
            exchangeRepository.cancelExchange(exchangeId)
            _state.update {
                it.copy(
                    currentExchangeId = null,
                    qrCodeBitmap = null,
                    selectedPokemon = null
                )
            }
        }
    }

    /**
     * Obtener propuesta de intercambio para aceptar
     */
    fun loadExchangeProposal(exchangeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val proposal = exchangeRepository.getExchangeProposal(exchangeId)

            if (proposal != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        proposalToAccept = proposal
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Propuesta no encontrada"
                    )
                }
            }
        }
    }
}

/**
 * Estado del sistema de intercambio
 */
data class ExchangeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userFavorites: List<FavoritePokemon> = emptyList(),
    val currentExchangeId: String? = null,
    val qrCodeBitmap: Bitmap? = null,
    val selectedPokemon: FavoritePokemon? = null,
    val proposalToAccept: ExchangeProposal? = null
)

/**
 * Eventos del sistema de intercambio
 */
sealed class ExchangeEvent {
    object ExchangeCompleted : ExchangeEvent()
    data class ExchangeFailed(val message: String) : ExchangeEvent()
}