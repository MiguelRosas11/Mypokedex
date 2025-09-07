package com.example.mypokedex.ui.features.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import java.util.Locale

class HomeViewModel(private val appContext: Context) : ViewModel() {

    private var currentPage = 0
    private val pageSize = 10

    private fun buildChunkFilename(page: Int = currentPage): String {
        val start = page * pageSize + 1
        val end = (page + 1) * pageSize
        return "pokemon_%03d_%d.json".format(Locale.US, start, end)
    }

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
            ),
            Pokemon(
                id = 6,
                name = "Charizard",
                types = listOf(PokemonType.FIRE, PokemonType.FLYING),
                height = 1.7f,
                weight = 90.5f,
                stats = listOf(
                    PokemonStat("HP", 78),
                    PokemonStat("Attack", 84),
                    PokemonStat("Defense", 78)
                )
            ),
            Pokemon(
                id = 9,
                name = "Blastoise",
                types = listOf(PokemonType.WATER),
                height = 1.6f,
                weight = 85.5f,
                stats = listOf(
                    PokemonStat("HP", 79),
                    PokemonStat("Attack", 83),
                    PokemonStat("Defense", 100)
                )
            ),
            Pokemon(
                id = 3,
                name = "Venusaur",
                types = listOf(PokemonType.GRASS, PokemonType.POISON),
                height = 2.0f,
                weight = 100.0f,
                stats = listOf(
                    PokemonStat("HP", 80),
                    PokemonStat("Attack", 82),
                    PokemonStat("Defense", 83)
                )
            ),
            Pokemon(
                id = 144,
                name = "Articuno",
                types = listOf(PokemonType.ICE, PokemonType.FLYING),
                height = 1.7f,
                weight = 55.4f,
                stats = listOf(
                    PokemonStat("HP", 90),
                    PokemonStat("Attack", 85),
                    PokemonStat("Defense", 100)
                )
            ),
            Pokemon(
                id = 131,
                name = "Lapras",
                types = listOf(PokemonType.WATER, PokemonType.ICE),
                height = 2.5f,
                weight = 220.0f,
                stats = listOf(
                    PokemonStat("HP", 130),
                    PokemonStat("Attack", 85),
                    PokemonStat("Defense", 80)
                )
            )
        )
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(context.applicationContext) as T
                }
            }
    }
}