package com.example.mypokedex.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.AuthRepository
import com.example.mypokedex.data.repository.FavoritesRepository
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repo: PokemonRepository,
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun load(idOrName: String) {
        _state.value = DetailState(isLoading = true)
        viewModelScope.launch {
            when (val res = repo.getDetail(idOrName)) {
                is Result.Success -> {
                    _state.value = DetailState(pokemon = res.data)
                    // Verificar si es favorito
                    checkIfFavorite(res.data.id)
                }
                is Result.Error -> {
                    _state.value = DetailState(error = res.message)
                }
                Result.Loading -> { }
            }
        }
    }

    private fun checkIfFavorite(pokemonId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val isFav = favoritesRepository.isFavorite(userId, pokemonId)
                _isFavorite.value = isFav
            }
        }
    }

    fun onToggleFavorite(pokemonId: Int, name: String, imageUrl: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            if (_isFavorite.value) {
                favoritesRepository.removeFavorite(userId, pokemonId)
                _isFavorite.value = false
            } else {
                favoritesRepository.addFavorite(userId, pokemonId, name, imageUrl)
                _isFavorite.value = true
            }
        }
    }
}