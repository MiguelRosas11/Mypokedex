package com.example.mypokedex.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.NavHostController
import com.example.mypokedex.ui.features.home.HomeScreen
import com.example.mypokedex.ui.features.home.HomeViewModel
import com.example.mypokedex.ui.features.home.SearchToolsDialog

@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel = HomeViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeScreen.route
    ) {
        // HOME
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onPokemonClick = { pokemonId ->
                    navController.navigate(AppScreens.DetailScreen.createRoute(pokemonId))
                },
                onOpenSearchTools = {
                    navController.navigate(AppScreens.SearchToolsDialog.route)
                }
            )
        }

        // DETAIL (ya parseas el argumento como String->Int en tu MainActivity original;
        // aquí lo dejamos igual de simple sin navArgument para no tocar tu modelo).
        composable(route = AppScreens.DetailScreen.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("pokemonId")?.toIntOrNull()
            if (id != null) {
                // Reusamos tu lógica actual: obtenemos el Pokémon desde el VM
                val pokemon = homeViewModel.getPokemonList().find { it.id == id }
                if (pokemon != null) {
                    com.example.mypokedex.ui.features.detail.DetailContainer(
                        pokemon = pokemon,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(AppScreens.HomeScreen.route) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(AppScreens.HomeScreen.route) { inclusive = true }
                }
            }
        }

        // DIALOG: SearchTools
        dialog(route = AppScreens.SearchToolsDialog.route) {
            SearchToolsDialog(
                onClose = { navController.popBackStack() }
            )
        }
    }
}
