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
                height = 0.7f,
                weight = 6.9f,
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
                id = 7,
                name = "Squirtle",
                types = listOf(PokemonType.WATER),
                height = 0.5f,
                weight = 9.0f,
                stats = listOf(
                    PokemonStat("HP", 44),
                    PokemonStat("Attack", 48),
                    PokemonStat("Defense", 65)
                )
            ),
            Pokemon(
                id = 25,
                name = "Pikachu",
                types = listOf(PokemonType.ELECTRIC),
                height = 0.4f,
                weight = 6.0f,
                stats = listOf(
                    PokemonStat("HP", 35),
                    PokemonStat("Attack", 55),
                    PokemonStat("Defense", 40)
                )
            )
        )
    }
}
