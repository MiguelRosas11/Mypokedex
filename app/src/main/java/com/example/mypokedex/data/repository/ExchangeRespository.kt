package com.example.mypokedex.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.Result
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ExchangeRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val favoritesRepository: FavoritesRepository // puedes no usarlo aquí
) {
    private val exchangesRef = firebaseDatabase.getReference("exchanges")
    // IMPORTANTE: asegúrate que tus favoritos realmente vivan en /favorites/{uid}/{pokeId}
    // Si los guardas en /users/{uid}/favorites/{pokeId}, cambia el acceso en executeAtomicExchange.
    private val favoritesRootPath = "favorites"

    // ---------- Crear propuesta ----------
    suspend fun createExchangeProposal(
        userAId: String,
        userAAlias: String,
        pokemonAId: Int,
        pokemonAName: String,
        pokemonAImageUrl: String
    ): kotlin.Result<String> = runCatching {
        val exchangeId = exchangesRef.push().key ?: error("No se pudo generar ID")
        val proposal = ExchangeProposal(
            id = exchangeId,
            userAId = userAId,
            userAAlias = userAAlias,
            pokemonAId = pokemonAId,
            pokemonAName = pokemonAName,
            pokemonAImageUrl = pokemonAImageUrl,
            status = ExchangeStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        exchangesRef.child(exchangeId).setValue(proposal).await()
        exchangeId
    }

    // ---------- Obtener propuesta ----------
    suspend fun getExchangeProposal(exchangeId: String): ExchangeProposal? = runCatching {
        exchangesRef.child(exchangeId).get().await().getValue(ExchangeProposal::class.java)
    }.getOrNull()

    // ---------- Observar propuesta ----------
    fun observeExchangeProposal(exchangeId: String): Flow<ExchangeProposal?> = callbackFlow {
        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(ExchangeProposal::class.java))
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        exchangesRef.child(exchangeId).addValueEventListener(l)
        awaitClose { exchangesRef.child(exchangeId).removeEventListener(l) }
    }

    // ---------- Aceptar (transacción atómica) ----------
    suspend fun acceptExchange(
        exchangeId: String,
        userBId: String,
        userBAlias: String,
        pokemonBId: Int,
        pokemonBName: String,
        pokemonBImageUrl: String
    ): kotlin.Result<Unit> = runCatching {
        val proposal = getExchangeProposal(exchangeId) ?: error("Propuesta no encontrada")

        if (proposal.status != ExchangeStatus.PENDING) error("Intercambio ya procesado")

        val now = System.currentTimeMillis()
        if (now - proposal.createdAt > 90_000) {
            exchangesRef.child(exchangeId).child("status").setValue(ExchangeStatus.EXPIRED.name).await()
            error("Intercambio expirado (90s)")
        }
        if (proposal.userAId == userBId) error("No puedes intercambiar contigo mismo.")

        // Transacción: valida existencia y mueve ambos Pokémon sin estados intermedios
        runAtomicExchange(
            exchangeId = exchangeId,
            userAId = proposal.userAId,
            userBId = userBId,
            userBAlias = userBAlias,
            pokemonAId = proposal.pokemonAId,
            pokemonAName = proposal.pokemonAName,
            pokemonAImageUrl = proposal.pokemonAImageUrl,
            pokemonBId = pokemonBId,
            pokemonBName = pokemonBName,
            pokemonBImageUrl = pokemonBImageUrl
        )
    }.onFailure { e ->
        // Si aborta la transacción, marca CANCELLED para que ambos vean el resultado coherente
        try { exchangesRef.child(exchangeId).child("status").setValue(ExchangeStatus.CANCELLED.name).await() } catch (_: Exception) {}
    }

    // ---------- Transacción en raíz ----------
    private suspend fun runAtomicExchange(
        exchangeId: String,
        userAId: String,
        userBId: String,
        userBAlias: String,
        pokemonAId: Int,
        pokemonAName: String,
        pokemonAImageUrl: String,
        pokemonBId: Int,
        pokemonBName: String,
        pokemonBImageUrl: String
    ) = suspendCancellableCoroutine<Unit> { cont ->
        val rootRef = firebaseDatabase.reference
        rootRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(current: MutableData): Transaction.Result {
                try {
                    val favRoot = current.child(favoritesRootPath)
                    val favA = favRoot.child(userAId)
                    val favB = favRoot.child(userBId)

                    val nodeA = favA.child(pokemonAId.toString())
                    val nodeB = favB.child(pokemonBId.toString())

                    val existsA = nodeA.value != null
                    val existsB = nodeB.value != null
                    if (!existsA || !existsB) return Transaction.abort()

                    // Elimina originales
                    nodeA.value = null
                    nodeB.value = null

                    // Inserta cruzado
                    val dataForA = mapOf(
                        "id" to pokemonBId,
                        "name" to pokemonBName,
                        "imageUrl" to pokemonBImageUrl,
                        "addedAt" to System.currentTimeMillis()
                    )
                    val dataForB = mapOf(
                        "id" to pokemonAId,
                        "name" to pokemonAName,
                        "imageUrl" to pokemonAImageUrl,
                        "addedAt" to System.currentTimeMillis()
                    )
                    favA.child(pokemonBId.toString()).value = dataForA
                    favB.child(pokemonAId.toString()).value = dataForB

                    // Actualiza propuesta a COMPLETED (y guarda info del B que aceptó)
                    val exNode = current.child("exchanges").child(exchangeId)
                    exNode.child("userBId").value = userBId
                    exNode.child("userBAlias").value = userBAlias
                    exNode.child("pokemonBId").value = pokemonBId
                    exNode.child("pokemonBName").value = pokemonBName
                    exNode.child("pokemonBImageUrl").value = pokemonBImageUrl
                    exNode.child("status").value = ExchangeStatus.COMPLETED.name
                    exNode.child("completedAt").value = System.currentTimeMillis()

                    return Transaction.success(current)
                } catch (_: Exception) {
                    return Transaction.abort()
                }
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    cont.resumeWithException(Exception("Error en transacción: ${error.message}"))
                } else if (!committed) {
                    cont.resumeWithException(Exception("Uno de los Pokémon ya no está disponible"))
                } else {
                    cont.resume(Unit)
                }
            }
        })
    }

    // ---------- Cancelar ----------
    suspend fun cancelExchange(exchangeId: String): Result<Void?> = runCatching {
        exchangesRef.child(exchangeId).child("status").setValue(ExchangeStatus.CANCELLED.name).await()
    }
}

// ------- Modelos -------
data class ExchangeProposal(
    val id: String = "",
    val userAId: String = "",
    val userAAlias: String = "",
    val pokemonAId: Int = 0,
    val pokemonAName: String = "",
    val pokemonAImageUrl: String = "",
    val userBId: String? = null,
    val userBAlias: String? = null,
    val pokemonBId: Int? = null,
    val pokemonBName: String? = null,
    val pokemonBImageUrl: String? = null,
    val status: ExchangeStatus = ExchangeStatus.PENDING,
    val createdAt: Long = 0,
    val completedAt: Long? = null
)

enum class ExchangeStatus { PENDING, COMPLETED, CANCELLED, EXPIRED }