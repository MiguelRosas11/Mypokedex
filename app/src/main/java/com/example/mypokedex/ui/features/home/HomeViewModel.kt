package com.example.mypokedex.ui.features.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.Locale

class HomeViewModel(private val appContext: Context) : ViewModel() {

    private var currentPage = 0
    private val pageSize = 10

    // Lista observable usando mutableStateListOf para mejor rendimiento
    private val _pokemonList = mutableStateListOf<Pokemon>()

    // JSON deserializer configurado para ser flexible
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        // Cargar los datos iniciales
        loadInitialData()
    }

    private fun loadInitialData() {
        // Cargar datos hardcodeados como base
        _pokemonList.addAll(getHardcodedPokemon())
        // Intentar cargar el primer chunk de JSON
        loadMorePokemon()
    }

    private fun buildChunkFilename(page: Int = currentPage): String {
        val start = page * pageSize + 1
        val end = (page + 1) * pageSize
        return "pokemon_%03d_%03d.json".format(Locale.US, start, end)
    }

    /**
     * Función pública que implementa la carga y decodificación de datos
     * según las especificaciones del laboratorio paso 3
     */
    fun loadMorePokemon() {
        // 1. Construir el nombre del siguiente archivo JSON a cargar
        val filename = buildChunkFilename(currentPage)

        // 2-3. Leer y decodificar el contenido usando la función auxiliar
        val newPokemon = loadPokemonFromJson(appContext, filename)

        // 4. Añadir los nuevos Pokémon a la lista observable usando addAll()
        if (newPokemon.isNotEmpty()) {
            _pokemonList.addAll(newPokemon)
            currentPage++
            println("Cargados ${newPokemon.size} Pokémon desde $filename. Total: ${_pokemonList.size}")
        } else {
            println("No se pudieron cargar más Pokémon desde $filename")
        }
    }

    /**
     * Lee un archivo JSON desde la carpeta 'assets', lo decodifica y devuelve una lista de objetos Pokemon.
     *
     * @param context El contexto de la aplicación, necesario para acceder a los assets.
     * @param fileName El nombre del archivo JSON que se encuentra en la carpeta 'assets'.
     * @return Una lista de objetos Pokemon, o una lista vacía si el archivo no se encuentra o hay un error.
     */
    private fun loadPokemonFromJson(context: Context, fileName: String): List<Pokemon> {
        return try {
            // 1. Abrir y leer el archivo desde 'assets' como un único String
            val jsonString: String = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            // 2. Extraer el array "items" del objeto JSON principal
            // kotlinx.serialization por sí solo no puede decodificar directamente un array anidado,
            // por lo que usamos la clase JSONObject nativa de Android para este paso intermedio
            val jsonObject = JSONObject(jsonString)
            val itemsArrayString = jsonObject.getJSONArray("items").toString()

            // 3. Decodificar el string del array "items" a una lista de Pokemon
            json.decodeFromString<List<Pokemon>>(itemsArrayString)

        } catch (e: FileNotFoundException) {
            // Manejo específico cuando se llega al final de los datos
            println("Archivo $fileName no encontrado. Se llegó al final de los datos.")
            emptyList()
        } catch (e: Exception) {
            // Si hay algún error (ej. JSON mal formado),
            // imprime el error y devuelve una lista vacía para evitar que la app falle
            println("Error cargando Pokemon desde $fileName: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun getPokemonList(): List<Pokemon> {
        return _pokemonList
    }

    private fun getHardcodedPokemon(): List<Pokemon> {
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