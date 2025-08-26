package com.example.mypokedex.data.model


enum class PokemonType{
    WATER,
    FIRE,
    GRASS,
    ELECTRIC,
    ROCK,
    PSYCHIC;
}


data class Pokemon(
    val id: Int,
    val name: String,
    val maxHP: Int,
    val type: PokemonType,
) {


    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

}



