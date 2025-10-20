package com.example.mypokedex.data.model

import com.example.mypokedex.data.remote.dto.PokemonDetailDto
import com.example.mypokedex.data.remote.dto.PokemonEntryDto

fun PokemonEntryDto.toPokemon(): Pokemon {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: -1
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = imageUrl,
        types = emptyList(),
        height = 0f,
        weight = 0f,
        stats = emptyList()
    )
}

fun PokemonDetailDto.toPokemon(): Pokemon {
    val imageUrl = sprites.other?.officialArtwork?.front_default
        ?: sprites.front_default
        ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = imageUrl,
        types = types.map { typeSlot ->
            try {
                PokemonType.valueOf(typeSlot.type.name.uppercase())
            } catch (e: IllegalArgumentException) {
                PokemonType.NORMAL
            }
        },
        height = height / 10f,  // Convertir decímetros a metros
        weight = weight / 10f,  // Convertir hectogramos a kilogramos
        stats = stats.map { statDto ->
            PokemonStat(
                name = statDto.stat.name.replaceFirstChar { it.uppercase() },
                value = statDto.base_stat
            )
        }
    )
}