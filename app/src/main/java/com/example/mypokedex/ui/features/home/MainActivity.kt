package com.example.mypokedex.ui.features.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mypokedex.data.model.Pokemon
import com.example.mypokedex.data.model.PokemonStat
import com.example.mypokedex.data.model.PokemonType
import com.example.mypokedex.navigation.AppScreens
import com.example.mypokedex.ui.features.detail.DetailContainer
import com.example.mypokedex.ui.theme.MypokedexTheme
import androidx.navigation.compose.dialog
import com.example.mypokedex.ui.features.home.SearchToolsDialog

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MypokedexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyPokedexApp(homeViewModel = homeViewModel)
                }
            }
        }
    }
}

/**
 * Composable principal que maneja toda la navegación de la aplicación
 */
@Composable
fun MyPokedexApp(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeScreen.route
    ) {
        // Pantalla principal (Home)
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onPokemonClick = { pokemonId ->
                    // Navegación al poke
                    navController.navigate(AppScreens.DetailScreen.createRoute(pokemonId))
                }
            )
        }

        // Pantalla de detalle
        composable(
            route = AppScreens.DetailScreen.route
        ) { backStackEntry ->
            // ahora se Extrae el ID del Poke de la navegacion
            val pokemonId = backStackEntry.arguments?.getString("pokemonId")?.toIntOrNull()

            if (pokemonId != null) {
                // se busca el poke en  el ViewModel
                val pokemon = homeViewModel.getPokemonList().find { it.id == pokemonId }

                if (pokemon != null) {
                    DetailContainer(
                        pokemon = pokemon,
                        onBack = {
                            // Navegar de vuelta a la pantalla anterior
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Poke no encontrado
                    // Por ahora, navegar de vuelta al inicio
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(AppScreens.HomeScreen.route) {
                            inclusive = true
                        }
                    }
                }
            } else {
                // ID inválido - navegar de vuelta al inicio
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(AppScreens.HomeScreen.route) {
                        inclusive = true
                    }
                }
            }
        }

        dialog(route = AppScreens.SearchToolsDialog.route) {
            SearchToolsDialog(
                onClose = { navController.popBackStack() }
            )
        }
    }
}