package com.example.mypokedex.data.local.entida

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Entidad de Room que representa un Pokémon en caché
 */
@Entity(tableName = "cached_pokemon")
@TypeConverters(Converters::class)
data class CachedPokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<PokemonType>,
    val height: Float,
    val weight: Float,
    val stats: List<PokemonStat>,
    val lastFetchedAt: Long = System.currentTimeMillis()
)

/**
 * Convertidores de tipos para Room
 */
class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Conversor para List<PokemonType>
    @TypeConverter
    fun fromPokemonTypeList(types: List<PokemonType>): String {
        val adapter: JsonAdapter<List<PokemonType>> = moshi.adapter(
            Types.newParameterizedType(List::class.java, PokemonType::class.java)
        )
        return adapter.toJson(types)
    }

    @TypeConverter
    fun toPokemonTypeList(json: String): List<PokemonType> {
        val adapter: JsonAdapter<List<PokemonType>> = moshi.adapter(
            Types.newParameterizedType(List::class.java, PokemonType::class.java)
        )
        return adapter.fromJson(json) ?: emptyList()
    }

    // Conversor para List<PokemonStat>
    @TypeConverter
    fun fromPokemonStatList(stats: List<PokemonStat>): String {
        val adapter: JsonAdapter<List<PokemonStat>> = moshi.adapter(
            Types.newParameterizedType(List::class.java, PokemonStat::class.java)
        )
        return adapter.toJson(stats)
    }

    @TypeConverter
    fun toPokemonStatList(json: String): List<PokemonStat> {
        val adapter: JsonAdapter<List<PokemonStat>> = moshi.adapter(
            Types.newParameterizedType(List::class.java, PokemonStat::class.java)
        )
        return adapter.fromJson(json) ?: emptyList()
    }
}

/**
 * Extensiones para convertir entre CachedPokemon y Pokemon
 */
fun CachedPokemon.toPokemon(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types,
        height = height,
        weight = weight,
        stats = stats
    )
}

fun Pokemon.toCachedPokemon(): CachedPokemon {
    return CachedPokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types,
        height = height,
        weight = weight,
        stats = stats,
        lastFetchedAt = System.currentTimeMillis()
    )
}