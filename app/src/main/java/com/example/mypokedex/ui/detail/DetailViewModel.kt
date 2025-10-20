package com.example.mypokedex.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val repo: PokemonRepository) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun load(idOrName: String) {
        _state.value = DetailState(isLoading = true)
        viewModelScope.launch {
            when (val res = repo.getDetail(idOrName)) {
                is Result.Success -> _state.value = DetailState(pokemon = res.data)
                is Result.Error -> _state.value = DetailState(error = res.message)
                else -> {}
            }
        }
    }
}

