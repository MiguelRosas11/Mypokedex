package com.example.mypokedex.data.model

import com.example.mypokedex.data.remote.dto.PokemonDetailDto
import com.example.mypokedex.data.remote.dto.PokemonEntryDto

fun PokemonEntryDto.toPokemon(): Pokemon {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: -1
    val img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = img
    )
}

fun PokemonDetailDto.toPokemon(): Pokemon =
    Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = sprites.front_default
    )