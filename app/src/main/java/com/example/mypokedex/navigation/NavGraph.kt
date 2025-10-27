package com.example.mypokedex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mypokedex.data.local.PreferencesDataStore
import com.example.mypokedex.data.local.PokemonDatabase
import com.example.mypokedex.data.network.NetworkMonitor
import com.example.mypokedex.data.remote.NetworkModule
import com.example.mypokedex.data.repository.PokemonRepository
import com.example.mypokedex.ui.detail.DetailScreen
import com.example.mypokedex.ui.detail.DetailViewModel
import com.example.mypokedex.ui.pokedex.PokedexScreen
import com.example.mypokedex.ui.pokedex.PokedexViewModel

sealed class Dest(val route: String) {
    data object Pokedex : Dest("pokedex")
    data object Detail : Dest("detail/{id}") { fun route(id: Int) = "detail/$id" }
}

@Composable
fun AppNav() {
    val context = LocalContext.current

    // Crear instancias de las dependencias
    val database = PokemonDatabase.getDatabase(context)
    val preferencesDataStore = PreferencesDataStore(context)
    val networkMonitor = NetworkMonitor(context)

    val repo = PokemonRepository(
        api = NetworkModule.api,
        dao = database.pokemonDao(),
        preferencesDataStore = preferencesDataStore,
        networkMonitor = networkMonitor
    )

    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Dest.Pokedex.route) {
        composable(Dest.Pokedex.route) {
            val vm: PokedexViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(c: Class<T>): T = PokedexViewModel(repo) as T
            })
            val uiState by vm.state.collectAsState()
            val isConnected by vm.isConnected.collectAsState()

            PokedexScreen(
                state = uiState,
                isConnected = isConnected,
                onSearch = vm::onSearch,
                onToggleSort = vm::toggleSortByName,
                onToggleSortDirection = vm::toggleSortDirection,
                onLoadMore = vm::loadNextPage,
                onRetry = vm::retry,
                onRefresh = vm::refresh,
                onOpen = { id -> nav.navigate(Dest.Detail.route(id)) }
            )
        }

        composable(
            Dest.Detail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id").toString()
            val vm: DetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(c: Class<T>): T = DetailViewModel(repo) as T
            })
            LaunchedEffect(id) { vm.load(id) }
            val uiState by vm.state.collectAsState()
            DetailScreen(state = uiState, onBack = { nav.popBackStack() })
        }
    }
}