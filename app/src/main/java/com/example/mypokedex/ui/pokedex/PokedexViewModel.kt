package com.example.mypokedex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokedexViewModel(private val repo: PokemonRepository) : ViewModel() {

    private val _state = MutableStateFlow(PokedexState())
    val state: StateFlow<PokedexState> = _state.asStateFlow()

    private var page = 0
    private var loading = false
    private var endReached = false
    private val cache = mutableListOf<Pokemon>()

    init { loadNextPage() }

    fun onSearch(text: String) {
        _state.update { it.copy(query = text) }
        applyFilters()
    }

    fun toggleSortByName() {
        _state.update { it.copy(sortByName = !it.sortByName) }
        applyFilters()
    }

    fun retry() = loadNextPage()

    fun loadNextPage() {
        if (loading || endReached) return
        loading = true
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val res = repo.getPage(page)) {
                is Result.Success -> {
                    val newItems = res.data
                    if (newItems.isEmpty()) {
                        endReached = true
                        _state.update { it.copy(isLoading = false, canLoadMore = false) }
                    } else {
                        cache.addAll(newItems)
                        page++
                        applyFilters(true)
                    }
                }
                is Result.Error -> _state.update { it.copy(isLoading = false, error = res.message) }
                else -> {}
            }
            loading = false
        }
    }

    private fun applyFilters(loadingDone: Boolean = false) {
        val q = state.value.query.trim().lowercase()
        var list = cache.toList()
        if (q.isNotEmpty()) list = list.filter { it.name.lowercase().contains(q) }
        list = if (state.value.sortByName) list.sortedBy { it.name } else list.sortedBy { it.id }

        _state.update {
            it.copy(
                items = list,
                isLoading = if (loadingDone) false else it.isLoading,
                canLoadMore = !endReached
            )
        }
    }
}
