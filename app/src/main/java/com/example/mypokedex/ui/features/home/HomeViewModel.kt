package com.example.mypokedex.ui.features.home

import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonType

class HomeViewModel {
    fun getPokemonList(): List<Pokemon>{
        return listOf(
            Pokemon(1, "Bulbasaur", 180, PokemonType.GRASS),
            Pokemon(4, "Charmander", 150, PokemonType.FIRE),
            Pokemon(150, "Mewtwo", 250, PokemonType.PSYCHIC)
        )
    }
}