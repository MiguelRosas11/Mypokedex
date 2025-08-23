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
    var numeroPokedex: Int = 0,
    val name: String,
    val maxHP: Int,
    val type: PokemonType
){
    var id: Int = 0
    init {
        id = getNextId()
    }


    companion object {
        private var idCounter = 0
        fun getNextId(): Int {
            idCounter++
            return idCounter
        }
    }

}

