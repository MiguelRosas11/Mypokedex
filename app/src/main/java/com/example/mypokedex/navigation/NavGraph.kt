package com.example.mypokedex.navigation

import androidx.compose.runtime.*
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
import com.example.mypokedex.data.repository.*
import com.example.mypokedex.ui.auth.AuthModal
import com.example.mypokedex.ui.detail.DetailScreen
import com.example.mypokedex.ui.detail.DetailViewModel
import com.example.mypokedex.ui.pokedex.PokedexScreen
import com.example.mypokedex.ui.pokedex.PokedexViewModel
import com.example.mypokedex.ui.exchange.ExchangeScreen
import com.example.mypokedex.ui.exchange.ExchangeViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


sealed class Dest(val route: String) {
    data object Pokedex : Dest("pokedex")
    data object Detail : Dest("detail/{id}") { fun route(id: Int) = "detail/$id" }
    data object Favorites : Dest("favorites")
    data object Exchange : Dest("exchange")
    data object AcceptExchange : Dest("exchange/accept/{exchangeId}") {
        fun route(exchangeId: String) = "exchange/accept/$exchangeId"
    }
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nav = rememberNavController()

    // Repositories
    val database = PokemonDatabase.getDatabase(context)
    val preferencesDataStore = PreferencesDataStore(context)
    val networkMonitor = NetworkMonitor(context)

    val pokemonRepo = PokemonRepository(
        api = NetworkModule.api,
        dao = database.pokemonDao(),
        preferencesDataStore = preferencesDataStore,
        networkMonitor = networkMonitor
    )

    val authRepo = remember { AuthRepository() }
    val favoritesRepo = remember { FavoritesRepository() }
    // ¡CAMBIO CLAVE AQUÍ!
    // Instancia la clase de implementación concreta, pasándole la dependencia que necesita (FirebaseDatabase)
    val exchangeRepo: ExchangeRepository = remember {
        ExchangeRepository(FirebaseDatabase.getInstance())
    }

    // Estado de autenticación
    val currentUser by authRepo.currentUser.collectAsState(initial = null)
    var showAuthModal by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Modal de autenticación
    if (showAuthModal) {
        AuthModal(
            onDismiss = {
                showAuthModal = false
                pendingAction = null
            },
            onAuthSuccess = { alias ->
                scope.launch {
                    val result = authRepo.signInWithAlias(alias)
                    when (result) {
                        is com.example.mypokedex.data.repository.Resource.Success -> {
                            showAuthModal = false
                            pendingAction?.invoke()
                            pendingAction = null
                        }
                        is com.example.mypokedex.data.repository.Resource.Error -> {
                            // Mostrar error
                        }
                    }
                }
            },
            onAuthError = { error ->
                // Manejar error
            }
        )
    }

    /**
     * Función helper para acciones que requieren autenticación
     */
    fun requireAuth(action: () -> Unit) {
        if (currentUser != null) {
            action()
        } else {
            pendingAction = action
            showAuthModal = true
        }
    }

    NavHost(navController = nav, startDestination = Dest.Pokedex.route) {
        // Pantalla principal Pokedex
        composable(Dest.Pokedex.route) {
            val vm: PokedexViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(c: Class<T>): T = PokedexViewModel(pokemonRepo) as T
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
                onOpen = { id -> nav.navigate(Dest.Detail.route(id)) },
                onOpenFavorites = {
                    requireAuth {
                        nav.navigate(Dest.Favorites.route)
                    }
                },
                onOpenExchange = {
                    requireAuth {
                        nav.navigate(Dest.Exchange.route)
                    }
                }
            )
        }

        // Pantalla de detalle
        composable(
            Dest.Detail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id").toString()
            val vm: DetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(c: Class<T>): T =
                    //  repositorios necesarios
                    DetailViewModel(pokemonRepo, authRepo, favoritesRepo) as T
            })
            LaunchedEffect(id) { vm.load(id) }
            val uiState by vm.state.collectAsState()

            DetailScreen(
                state = uiState,
                onBack = { nav.popBackStack() },
                onToggleFavorite = { pokemonId, name, imageUrl ->
                    // La UI solo notifica al ViewModel
                    requireAuth {
                        vm.onToggleFavorite(pokemonId, name, imageUrl)
                    }
                }
            )
        }


        //composable de ejemplo para favoritos
        composable(Dest.Favorites.route) {
            // Aquí llamar a tu FavoritesScreen, por ahora un placeholder:
            // Por ejemplo: FavoritesScreen(navController = nav)
            // Text("Pantalla de Favoritos")
        }



        // Pantalla de intercambio
        composable(Dest.Exchange.route) {
            if (currentUser == null) {
                // Mostrar modal de auth
                LaunchedEffect(Unit) {
                    showAuthModal = true
                }
            } else {
                val vm: ExchangeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(c: Class<T>): T =
                        ExchangeViewModel(authRepo, favoritesRepo, exchangeRepo) as T
                })

                val uiState by vm.state.collectAsState()
                val userId = currentUser!!.uid

                // Aquí necesitarías obtener el alias del usuario
                // Por simplicidad, usamos los primeros 6 chars del UID
                val userAlias = userId.take(6).uppercase()

                ExchangeScreen(
                    favorites = uiState.userFavorites,
                    currentUserId = userId,
                    currentUserAlias = userAlias,
                    onBack = { nav.popBackStack() },
                    onCreateProposal = { pokemon ->
                        vm.createExchangeProposal(pokemon, userAlias)
                    },
                    onScanQR = {
                        // Implementar escaneo de QR
                        // Por ahora, navegar a pantalla de aceptación
                    }
                )
            }
        }
    }
}