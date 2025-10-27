package com.example.mypokedex.data.local.dao

import androidx.room.*
import com.example.mypokedex.data.local.entida.CachedPokemon
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a los Pokémon en caché
 */
@Dao
interface PokemonDao {

    /**
     * Obtiene todos los Pokémon ordenados por ID ascendente
     */
    @Query("SELECT * FROM cached_pokemon ORDER BY id ASC")
    fun getAllPokemonByIdAsc(): Flow<List<CachedPokemon>>

    /**
     * Obtiene todos los Pokémon ordenados por ID descendente
     */
    @Query("SELECT * FROM cached_pokemon ORDER BY id DESC")
    fun getAllPokemonByIdDesc(): Flow<List<CachedPokemon>>

    /**
     * Obtiene todos los Pokémon ordenados por nombre ascendente
     */
    @Query("SELECT * FROM cached_pokemon ORDER BY name ASC")
    fun getAllPokemonByNameAsc(): Flow<List<CachedPokemon>>

    /**
     * Obtiene todos los Pokémon ordenados por nombre descendente
     */
    @Query("SELECT * FROM cached_pokemon ORDER BY name DESC")
    fun getAllPokemonByNameDesc(): Flow<List<CachedPokemon>>

    /**
     * Obtiene un Pokémon específico por ID
     */
    @Query("SELECT * FROM cached_pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): CachedPokemon?

    /**
     * Inserta o actualiza un Pokémon
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: CachedPokemon)

    /**
     * Inserta o actualiza una lista de Pokémon
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemonList: List<CachedPokemon>)

    /**
     * Elimina todos los Pokémon
     */
    @Query("DELETE FROM cached_pokemon")
    suspend fun clearAll()

    /**
     * Obtiene el conteo total de Pokémon en caché
     */
    @Query("SELECT COUNT(*) FROM cached_pokemon")
    suspend fun getPokemonCount(): Int

    /**
     * Verifica si el caché está vacío
     */
    @Query("SELECT COUNT(*) > 0 FROM cached_pokemon")
    suspend fun hasCachedPokemon(): Boolean

    /**
     * Busca Pokémon por nombre
     */
    @Query("SELECT * FROM cached_pokemon WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPokemon(query: String): Flow<List<CachedPokemon>>
}