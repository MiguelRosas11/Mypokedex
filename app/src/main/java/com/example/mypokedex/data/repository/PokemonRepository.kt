package com.example.mypokedex.data.repository

import com.example.mypokedex.data.local.PreferencesDataStore
import com.example.mypokedex.data.local.dao.PokemonDao
import com.example.mypokedex.data.local.entida.toCachedPokemon
import com.example.mypokedex.data.local.entida.toPokemon
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.toPokemon
import com.example.mypokedex.data.network.NetworkMonitor
import com.example.mypokedex.data.remote.PokeApi
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository actualizado que gestiona:
 * - Caché local con Room
 * - Preferencias con DataStore
 * - Estado de red con NetworkMonitor
 */
class PokemonRepository(
    private val api: PokeApi,
    private val dao: PokemonDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val networkMonitor: NetworkMonitor
) {
    private val pageSize = 30

    /**
     * Flow de estado de red
     */
    val isConnected: Flow<Boolean> = networkMonitor.isConnected

    /**
     * Flow de preferencias de ordenamiento
     */
    val sortOrderFlow: Flow<Pair<String, String>> = preferencesDataStore.sortOrderFlow

    /**
     * Guarda las preferencias de ordenamiento
     */
    suspend fun saveSortOrder(sortBy: String, direction: String) {
        preferencesDataStore.saveSortOrder(sortBy, direction)
    }

    /**
     * Obtiene Pokémon desde el caché local según el orden especificado
     */
    fun getPokemonFromCache(sortBy: String, direction: String): Flow<List<Pokemon>> {
        val cachedFlow = when {
            sortBy == "ID" && direction == "ASC" -> dao.getAllPokemonByIdAsc()
            sortBy == "ID" && direction == "DESC" -> dao.getAllPokemonByIdDesc()
            sortBy == "NAME" && direction == "ASC" -> dao.getAllPokemonByNameAsc()
            sortBy == "NAME" && direction == "DESC" -> dao.getAllPokemonByNameDesc()
            else -> dao.getAllPokemonByIdAsc()
        }

        return cachedFlow.map { cachedList ->
            cachedList.map { it.toPokemon() }
        }
    }

    /**
     * Sincroniza datos desde la API y los guarda en caché
     * Solo se ejecuta cuando hay conexión a Internet
     */
    suspend fun syncPokemonData(forceRefresh: Boolean = false): Result<Unit> {
        // Verificar si hay conexión
        if (!networkMonitor.isConnectedNow()) {
            return Result.Error("Sin conexión a Internet")
        }

        // Si no es forzado y ya tenemos datos en caché, no sincronizar
        if (!forceRefresh && dao.hasCachedPokemon()) {
            return Result.Success(Unit)
        }

        return try {
            // Obtener múltiples páginas para tener un buen conjunto de datos
            val allPokemon = mutableListOf<Pokemon>()

            for (page in 0..4) { // Primeros 150 Pokémon (5 páginas de 30)
                val offset = page * pageSize
                val dto = api.getPokemonPage(limit = pageSize, offset = offset)
                val pokemonList = dto.results.map { it.toPokemon() }
                allPokemon.addAll(pokemonList)
            }

            // Guardar en caché
            dao.insertAll(allPokemon.map { it.toCachedPokemon() })

            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error("Error de red: ${e.message}")
        } catch (e: HttpException) {
            Result.Error("Error del servidor: ${e.code()}")
        } catch (e: Exception) {
            Result.Error("Error inesperado: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene detalle de un Pokémon
     * Intenta desde la API si hay conexión, sino desde caché
     */
    suspend fun getDetail(idOrName: String): Result<Pokemon> {
        // Primero intentar desde caché
        val cachedPokemon = try {
            val id = idOrName.toIntOrNull()
            if (id != null) {
                dao.getPokemonById(id)?.toPokemon()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        // Si hay conexión, intentar actualizar desde API
        if (networkMonitor.isConnectedNow()) {
            return try {
                val pokemon = api.getPokemonDetail(idOrName).toPokemon()
                // Guardar en caché
                dao.insertPokemon(pokemon.toCachedPokemon())
                Result.Success(pokemon)
            } catch (e: IOException) {
                // Si falla la red pero tenemos caché, usar el caché
                cachedPokemon?.let { Result.Success(it) }
                    ?: Result.Error("Sin conexión y no hay datos en caché")
            } catch (e: HttpException) {
                cachedPokemon?.let { Result.Success(it) }
                    ?: Result.Error("Pokémon no encontrado")
            } catch (e: Exception) {
                Result.Error("Error: ${e.localizedMessage}")
            }
        }

        // Sin conexión, usar solo caché
        return cachedPokemon?.let { Result.Success(it) }
            ?: Result.Error("Sin conexión y no hay datos en caché")
    }

    /**
     * Verifica si hay datos en caché
     */
    suspend fun hasCachedData(): Boolean {
        return dao.hasCachedPokemon()
    }

    /**
     * Limpia el caché
     */
    suspend fun clearCache() {
        dao.clearAll()
    }
}