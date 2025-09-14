package com.example.mypokedex.navigation

/**
 * Objeto sellado que define todas las pantallas/rutas de navegación de la aplicación.
 * Este patrón permite definir de forma type-safe todas las rutas disponibles.
 */
sealed class AppScreens(val route: String) {

    /**
     * Pantalla principal que muestra la lista de Pokémon con funcionalidad de búsqueda
     */
    object HomeScreen : AppScreens("home_screen")

    /**
     * Pantalla de detalle que muestra información completa de un Pokémon específico.
     * Requiere el ID del Pokémon como argumento de navegación.
     *
     * @param pokemonId ID del Pokémon a mostrar
     */
    object DetailScreen : AppScreens("detail_screen/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "detail_screen/$pokemonId"
    }

    /**
     * Diálogo de herramientas de búsqueda/ordenamiento (si se implementa como pantalla)
     */
    object SearchToolsDialog : AppScreens("search_tools_dialog")
}