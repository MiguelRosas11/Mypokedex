package com.example.mypokedex.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.AuthRepository
import com.example.mypokedex.data.repository.FavoritesRepository
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.data.repository.Resource
import com.example.mypokedex.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repo: PokemonRepository,
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun load(idOrName: String) {
        _state.value = DetailState(isLoading = true)
        viewModelScope.launch {
            when (val res = repo.getDetail(idOrName)) {
                // Cambia Result por Resource aquí
                is Result.Success -> {
                    _state.value = DetailState(pokemon = res.data)
                }
                is Result.Error -> {
                    _state.value = DetailState(error = res.message)
                }

                Result.Loading -> TODO()
            }
        }
    }

    fun onToggleFavorite(pokemonId: Int, name: String, imageUrl: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch // Obtener userId

            if (favoritesRepository.isFavorite(userId, pokemonId)) {
                favoritesRepository.removeFavorite(userId, pokemonId)
            } else {
                favoritesRepository.addFavorite(userId, pokemonId, name, imageUrl)
            }
        }
    }



}

