package com.example.mypokedex.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<PokemonType> = emptyList(),
    val height: Float = 0f,   // en metros
    val weight: Float = 0f,   // en kg
    val stats: List<PokemonStat> = emptyList()
)

enum class PokemonType {
    NORMAL, FIRE, WATER, GRASS, ELECTRIC, FIGHTING, POISON, GROUND,
    FLYING, PSYCHIC, BUG, ROCK, GHOST, DARK, DRAGON, STEEL, FAIRY, ICE
}

data class PokemonStat(
    val name: String,   // "HP", "Attack", etc.
    val value: Int
)