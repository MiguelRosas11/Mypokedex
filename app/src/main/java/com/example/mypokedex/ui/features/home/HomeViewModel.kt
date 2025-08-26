package com.example.mypokedex.ui.features.home

import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType

class HomeViewModel {
    fun getPokemonList(): List<Pokemon> {
        return listOf(
            Pokemon(
                id = 1,
                name = "Bulbasaur",
                types = listOf(PokemonType.GRASS, PokemonType.POISON),
                height = 0.7f,   // m
                weight = 6.9f,   // kg
                stats = listOf(
                    PokemonStat("HP", 45),
                    PokemonStat("Attack", 49),
                    PokemonStat("Defense", 49)
                )
            ),
            Pokemon(
                id = 4,
                name = "Charmander",
                types = listOf(PokemonType.FIRE),
                height = 0.6f,
                weight = 8.5f,
                stats = listOf(
                    PokemonStat("HP", 39),
                    PokemonStat("Attack", 52),
                    PokemonStat("Defense", 43)
                )
            ),
            Pokemon(
                id = 150,
                name = "Mewtwo",
                types = listOf(PokemonType.PSYCHIC),
                height = 2.0f,
                weight = 122.0f,
                stats = listOf(
                    PokemonStat("HP", 106),
                    PokemonStat("Attack", 110),
                    PokemonStat("Defense", 90)
                )
            )
        )
    }
}