package com.example.mypokedex.data.model


enum class PokemonType{
    WATER,
    FIRE,
    GRASS,
    ELECTRIC,
    ROCK;
}


data class Pokemon(
    var id: Int = 0,
    val nombre: String,
    val maxHP: Int,
    val type: PokemonType
){
    companion object{
        private var idCounter = 0

        fun CreatePokemon(nombre: String, maxHP: Int, type: PokemonType): Pokemon {
            idCounter++
            return Pokemon(idCounter, nombre, maxHP, type)
        }
    }

}

