package com.example.mypokedex.navigation

/**
 * Objeto sellado que define todas las pantallas de navegación de la aplicación.
 */
sealed class AppScreens(val route: String) {

    /**
     * Pantalla principal que muestra la lista de Pokémon con funcionalidad de ña busqueda
     */
    object HomeScreen : AppScreens("home_screen")

    /**
     * Pantalla de detalle que muestra información  de un Poke específico.
     * este pide el ID del Poke como argumento de navegación.
     *
     * @param pokemonId ID del Poke a mostrar
     */
    object DetailScreen : AppScreens("detail_screen/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "detail_screen/$pokemonId"
    }

    /**
     * para herramientas de búsqueda (si se implementa como pantalla)
     */
    object SearchToolsDialog : AppScreens("search_tools_dialog")
}