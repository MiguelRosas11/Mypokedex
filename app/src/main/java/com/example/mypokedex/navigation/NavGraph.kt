package com.example.mypokedex.navigation

import android.widget.Toast
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
import com.example.mypokedex.ui.exchange.AcceptExchangeScreen
import com.example.mypokedex.ui.exchange.ExchangeEvent
import com.example.mypokedex.ui.pokedex.PokedexScreen
import com.example.mypokedex.ui.pokedex.PokedexViewModel
import com.example.mypokedex.ui.exchange.ExchangeScreen
import com.example.mypokedex.ui.exchange.ExchangeViewModel
import com.example.mypokedex.ui.exchange.QRExchangeDialog
import com.example.mypokedex.ui.favorites.FavoritesScreen
import com.example.mypokedex.ui.favorites.FavoritesViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

sealed class Dest(val route: String) {
    data object Pokedex : Dest("pokedex")
    data object Detail : Dest("detail/{id}") {
        fun route(id: Int) = "detail/$id"
    }
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

    // !! CORREGIDO: Inicializar repositorios en el orden correcto
    val userRepo = remember { UserRepository() }
    val authRepo = remember { AuthRepository(userRepo) } // userRepo se inyecta en authRepo
    val favoritesRepo = remember { FavoritesRepository() }
    // !! CORREGIDO: Inyectar favoritesRepo en exchangeRepo
    val exchangeRepo = remember { ExchangeRepository(FirebaseDatabase.getInstance(), favoritesRepo) }

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
                        is Resource.Success -> {
                            showAuthModal = false
                            pendingAction?.invoke()
                            pendingAction = null
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onAuthError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
            val vm: PokedexViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(c: Class<T>): T =
                        PokedexViewModel(pokemonRepo) as T
                }
            )
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
        ) { backStackEntry ->
            val id = backStackEntry.arguments!!.getInt("id").toString()
            val vm: DetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(c: Class<T>): T =
                        DetailViewModel(pokemonRepo, authRepo, favoritesRepo) as T
                }
            )

            LaunchedEffect(id) {
                vm.load(id)
            }

            val uiState by vm.state.collectAsState()
            val isFavorite by vm.isFavorite.collectAsState()

            DetailScreen(
                state = uiState,
                isFavorite = isFavorite,
                onBack = { nav.popBackStack() },
                onToggleFavorite = {
                    requireAuth {
                        val pokemon = uiState.pokemon
                        if (pokemon != null) {
                            vm.onToggleFavorite(pokemon.id, pokemon.name, pokemon.imageUrl)
                        }
                    }
                }
            )
        }

        // Pantalla de favoritos
        composable(Dest.Favorites.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    nav.popBackStack()
                    showAuthModal = true
                }
            } else {
                val vm: FavoritesViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(c: Class<T>): T =
                            FavoritesViewModel(authRepo, favoritesRepo) as T
                    }
                )

                val state by vm.state.collectAsState()

                FavoritesScreen(
                    favorites = state.favorites,
                    isLoading = state.isLoading,
                    error = state.error,
                    onBack = { nav.popBackStack() },
                    onPokemonClick = { pokemonId ->
                        nav.navigate(Dest.Detail.route(pokemonId))
                    },
                    onRemoveFavorite = { pokemonId ->
                        vm.removeFavorite(pokemonId)
                    },
                    onRetry = { vm.retry() }
                )
            }
        }

        // Pantalla de intercambio (Usuario B - Oferente)
        composable(Dest.Exchange.route) {
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    nav.popBackStack()
                    showAuthModal = true
                }
            } else {
                val vm: ExchangeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(c: Class<T>): T =
                            ExchangeViewModel(authRepo, favoritesRepo, exchangeRepo) as T
                    }
                )

                val uiState by vm.state.collectAsState()
                val userId = currentUser!!.uid

                var userAlias by remember { mutableStateOf(userId.take(6).uppercase()) }
                LaunchedEffect(userId) {
                    val alias = authRepo.getCurrentUserAlias()
                    if (alias != null) {
                        userAlias = alias
                    }
                }

                LaunchedEffect(Unit) {
                    vm.event.collect { event ->
                        when (event) {
                            is ExchangeEvent.ExchangeCompleted -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                                nav.popBackStack()
                            }
                            is ExchangeEvent.ExchangeFailed -> {
                                Toast.makeText(context, event.error, Toast.LENGTH_SHORT).show()
                                // No saques al usuario, el VM resetea el QR
                            }
                        }
                    }
                }

                if (uiState.currentExchangeId != null && uiState.qrCodeBitmap != null) {
                    QRExchangeDialog(
                        qrBitmap = uiState.qrCodeBitmap,
                        exchangeId = uiState.currentExchangeId!!,
                        pokemonName = uiState.selectedPokemon?.name ?: "",
                        onDismiss = { vm.cancelExchange() },
                        onTimeout = { vm.cancelExchange() }
                    )
                }

                ExchangeScreen(
                    favorites = uiState.userFavorites,
                    currentUserId = userId,
                    currentUserAlias = userAlias,
                    onBack = { nav.popBackStack() },
                    onCreateProposal = { pokemon ->
                        vm.createExchangeProposal(pokemon, userAlias)
                    },
                    onScanQR = { exchangeId ->
                        nav.navigate(Dest.AcceptExchange.route(exchangeId))
                    }
                )
            }
        }

        // Pantalla de aceptar intercambio (Usuario A - Aceptante)
        composable(
            Dest.AcceptExchange.route,
            arguments = listOf(navArgument("exchangeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exchangeId = backStackEntry.arguments?.getString("exchangeId")

            if (currentUser == null || exchangeId == null) {
                LaunchedEffect(Unit) {
                    nav.popBackStack()
                }
            } else {
                val vm: ExchangeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(c: Class<T>): T =
                            ExchangeViewModel(authRepo, favoritesRepo, exchangeRepo) as T
                    }
                )

                LaunchedEffect(exchangeId) {
                    vm.loadExchangeProposal(exchangeId)
                }

                val uiState by vm.state.collectAsState()
                val userId = currentUser!!.uid

                var userAlias by remember { mutableStateOf(userId.take(6).uppercase()) }
                LaunchedEffect(userId) {
                    val alias = authRepo.getCurrentUserAlias()
                    if (alias != null) {
                        userAlias = alias
                    }
                }

                LaunchedEffect(Unit) {
                    vm.event.collect { event ->
                        when (event) {
                            is ExchangeEvent.ExchangeCompleted -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                                nav.popBackStack(Dest.Favorites.route, inclusive = false)
                            }
                            is ExchangeEvent.ExchangeFailed -> {
                                // El error se muestra en el AlertDialog
                            }
                        }
                    }
                }

                AcceptExchangeScreen(
                    proposalPokemon = uiState.proposalToAccept?.let { proposal ->
                        FavoritePokemon(
                            id = proposal.pokemonAId,
                            name = proposal.pokemonAName,
                            imageUrl = proposal.pokemonAImageUrl,
                            addedAt = proposal.createdAt
                        )
                    },
                    userFavorites = uiState.userFavorites,
                    isLoading = uiState.isLoading,
                    error = uiState.exchangeError, // Pasa el error
                    onBack = { nav.popBackStack() },
                    onAccept = { pokemon ->
                        vm.acceptExchange(exchangeId, userAlias, pokemon)
                    },
                    onErrorDismiss = { vm.clearError() } // Limpia el error
                )
            }
        }
    }
}