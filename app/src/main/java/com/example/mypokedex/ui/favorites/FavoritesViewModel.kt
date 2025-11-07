package com.example.mypokedex.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.AuthRepository
import com.example.mypokedex.data.repository.FavoritePokemon
import com.example.mypokedex.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Usuario no autenticado"
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                favoritesRepository.getUserFavorites(userId)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.localizedMessage ?: "Error desconocido",
                                favorites = emptyList()
                            )
                        }
                    }
                    .collect { favorites ->
                        _state.update {
                            it.copy(
                                favorites = favorites,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al cargar favoritos"
                    )
                }
            }
        }
    }

    fun removeFavorite(pokemonId: Int) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                val success = favoritesRepository.removeFavorite(userId, pokemonId)
                if (!success) {
                    _state.update {
                        it.copy(error = "No se pudo eliminar el favorito")
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al eliminar: ${e.localizedMessage}")
                }
            }
        }
    }

    fun retry() {
        loadFavorites()
    }
}

data class FavoritesState(
    val favorites: List<FavoritePokemon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)