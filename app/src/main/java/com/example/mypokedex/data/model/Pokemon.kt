package com.example.mypokedex.data.model


data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val height: Float,   // en metros
    val weight: Float,   // en kg
    val stats: List<PokemonStat>
) {
    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}

enum class PokemonType {
    NORMAL, FIRE, WATER, GRASS, ELECTRIC, FIGHTING, POISON, GROUND,
    FLYING, PSYCHIC, BUG, ROCK, GHOST, DARK, DRAGON, STEEL, FAIRY, ICE
}

data class PokemonStat(
    val name: String,   // "HP", "Attack", etc.
    val value: Int
)


