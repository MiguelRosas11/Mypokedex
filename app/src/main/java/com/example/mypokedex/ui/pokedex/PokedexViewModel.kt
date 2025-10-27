package com.example.mypokedex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel actualizado para este lab 12 ya que  ahora  gestiona:
 * - Caché local
 * - Preferencias de ordenamiento
 * - Estado de red
 */
class PokedexViewModel(private val repo: PokemonRepository) : ViewModel() {

    private val _state = MutableStateFlow(PokedexState())
    val state: StateFlow<PokedexState> = _state.asStateFlow()

    // Estado de conexión
    val isConnected: StateFlow<Boolean> = repo.isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Query de búsqueda
    private val searchQuery = MutableStateFlow("")

    init {
        // Observar cambios en las preferencias y actualizar la lista
        viewModelScope.launch {
            combine(
                repo.sortOrderFlow,
                searchQuery,
                isConnected
            ) { sortOrder, query, connected ->
                Triple(sortOrder, query, connected)
            }.collectLatest { (sortOrder, query, connected) ->
                val (sortBy, direction) = sortOrder
                loadPokemonFromCache(sortBy, direction, query)

                // Si hay conexión y no tenemos datos, sincronizar
                if (connected && !repo.hasCachedData()) {
                    syncData()
                }
            }
        }

        // Sincronizar datos inicialmente si hay conexión
        viewModelScope.launch {
            if (isConnected.value) {
                syncData()
            }
        }
    }

    /**
     * Carga Pokémon desde el caché aplicando filtros y ordenamiento
     */
    private fun loadPokemonFromCache(sortBy: String, direction: String, query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repo.getPokemonFromCache(sortBy, direction)
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar datos: ${e.message}"
                        )
                    }
                }
                .collectLatest { pokemonList ->
                    val filtered = if (query.isBlank()) {
                        pokemonList
                    } else {
                        pokemonList.filter {
                            it.name.lowercase().contains(query.lowercase())
                        }
                    }

                    _state.update {
                        it.copy(
                            items = filtered,
                            isLoading = false,
                            error = null,
                            canLoadMore = false, // Ya no usamos paginación con Room
                            sortByName = sortBy == "NAME"
                        )
                    }
                }
        }
    }

    /**
     * Sincroniza datos desde la API
     */
    private fun syncData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repo.syncPokemonData(forceRefresh)) {
                is Result.Success -> {
                    // Los datos se actualizarán automáticamente por el Flow
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = if (repo.hasCachedData()) null else result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Actualiza la query de búsqueda
     */
    fun onSearch(text: String) {
        searchQuery.value = text
        _state.update { it.copy(query = text) }
    }

    /**
     * Cambia el ordenamiento
     */
    fun toggleSortByName() {
        viewModelScope.launch {
            val currentSort = repo.sortOrderFlow.first()
            val (currentSortBy, currentDirection) = currentSort

            val newSortBy = if (currentSortBy == "ID") "NAME" else "ID"

            repo.saveSortOrder(newSortBy, currentDirection)
        }
    }

    /**
     * Cambia la dirección del ordenamiento
     */
    fun toggleSortDirection() {
        viewModelScope.launch {
            val currentSort = repo.sortOrderFlow.first()
            val (currentSortBy, currentDirection) = currentSort

            val newDirection = if (currentDirection == "ASC") "DESC" else "ASC"

            repo.saveSortOrder(currentSortBy, newDirection)
        }
    }

    /**
     * Fuerza una sincronización desde la API
     */
    fun refresh() {
        if (isConnected.value) {
            syncData(forceRefresh = true)
        } else {
            _state.update { it.copy(error = "Sin conexión a Internet") }
        }
    }

    /**
     * No necesitamos loadNextPage porque Room maneja toda la lista
     */
    fun loadNextPage() {
        // No hace ni rosca en esta implementación
    }

    /**
     * Reintenta la carga
     */
    fun retry() {
        refresh()
    }
}