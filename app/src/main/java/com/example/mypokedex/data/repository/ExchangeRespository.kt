package com.example.mypokedex.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository para gestionar intercambios de Pokémon
 *  CORREGIDO: Solo recibe FirebaseDatabase como parámetro
 */
class ExchangeRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val exchangesRef = firebaseDatabase.getReference("exchanges")
    private val favoritesRef = firebaseDatabase.getReference("favorites")

    /**
     * Crear propuesta de intercambio
     */
    suspend fun createExchangeProposal(
        userAId: String,
        userAAlias: String,
        pokemonAId: Int,
        pokemonAName: String,
        pokemonAImageUrl: String
    ): kotlin.Result<String> {
        return try {
            val exchangeId = exchangesRef.push().key
                ?: return kotlin.Result.failure(Exception("Error al generar ID"))

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
            kotlin.Result.success(exchangeId)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    /**
     * Obtener propuesta de intercambio
     */
    suspend fun getExchangeProposal(exchangeId: String): ExchangeProposal? {
        return try {
            val snapshot = exchangesRef.child(exchangeId).get().await()
            snapshot.getValue(ExchangeProposal::class.java)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Observar cambios en una propuesta de intercambio
     */
    fun observeExchangeProposal(exchangeId: String): Flow<ExchangeProposal?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val proposal = snapshot.getValue(ExchangeProposal::class.java)
                trySend(proposal)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        exchangesRef.child(exchangeId).addValueEventListener(listener)
        awaitClose { exchangesRef.child(exchangeId).removeEventListener(listener) }
    }

    /**
     * Aceptar intercambio y ejecutar transacción
     */
    suspend fun acceptExchange(
        exchangeId: String,
        userBId: String,
        userBAlias: String,
        pokemonBId: Int,
        pokemonBName: String,
        pokemonBImageUrl: String
    ): kotlin.Result<Unit> {
        return try {
            val proposal = getExchangeProposal(exchangeId)
                ?: return kotlin.Result.failure(Exception("Propuesta no encontrada"))

            if (proposal.status != ExchangeStatus.PENDING) {
                return kotlin.Result.failure(Exception("Intercambio ya procesado"))
            }

            val now = System.currentTimeMillis()
            if (now - proposal.createdAt > 90_000) {
                exchangesRef.child(exchangeId)
                    .child("status")
                    .setValue(ExchangeStatus.EXPIRED.name)
                    .await()
                return kotlin.Result.failure(Exception("Intercambio expirado"))
            }

            //  Verificar que ambos usuarios aún tengan los Pokémon seleccionados
            val missingForUserA = !favoriteExists(proposal.userAId, proposal.pokemonAId)
            val missingForUserB = !favoriteExists(userBId, pokemonBId)

            if (missingForUserA || missingForUserB) {
                exchangesRef.child(exchangeId)
                    .child("status")
                    .setValue(ExchangeStatus.CANCELLED.name)
                    .await()

                val otherAlias = proposal.userAAlias.ifBlank { "El otro entrenador" }
                val message = when {
                    missingForUserA && missingForUserB ->
                        "El intercambio ya no es válido porque ninguno de los entrenadores conserva los Pokémon seleccionados."
                    missingForUserA ->
                        "$otherAlias ya no tiene a ${proposal.pokemonAName} en sus favoritos."
                    else ->
                        "Ya no tienes a $pokemonBName en tus favoritos."
                }

                return kotlin.Result.failure(IllegalStateException(message))
            }

            //  Transacción atómica
            executeAtomicExchange(
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

            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    /**
     *  Verificar si un favorito existe
     */
    private suspend fun favoriteExists(userId: String, pokemonId: Int): Boolean {
        val snap = favoritesRef.child(userId).child(pokemonId.toString()).get().await()
        return snap.exists()
    }

    /**
     *  Transacción atómica en Realtime Database
     * Modifica favorites y exchanges en una sola operación
     */
    private suspend fun executeAtomicExchange(
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
    ) {
        suspendCancellableCoroutine<Unit> { cont ->
            val rootRef = firebaseDatabase.reference

            rootRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    return try {
                        val favs = currentData.child("favorites")

                        // Funciones auxiliares para manipular favoritos
                        fun removeFavorite(u: String, p: Int) {
                            favs.child(u).child(p.toString()).value = null
                        }

                        fun addFavorite(u: String, p: Int, name: String, img: String) {
                            favs.child(u).child(p.toString()).value = mapOf(
                                "id" to p,
                                "name" to name,
                                "imageUrl" to img,
                                "addedAt" to System.currentTimeMillis()
                            )
                        }

                        //  Intercambiar Pokémon
                        removeFavorite(userAId, pokemonAId)
                        removeFavorite(userBId, pokemonBId)
                        addFavorite(userAId, pokemonBId, pokemonBName, pokemonBImageUrl)
                        addFavorite(userBId, pokemonAId, pokemonAName, pokemonAImageUrl)

                        // ctualizar estado del intercambio
                        val exchPath = currentData.child("exchanges").child(exchangeId)
                        exchPath.child("userBId").value = userBId
                        exchPath.child("userBAlias").value = userBAlias
                        exchPath.child("pokemonBId").value = pokemonBId
                        exchPath.child("pokemonBName").value = pokemonBName
                        exchPath.child("pokemonBImageUrl").value = pokemonBImageUrl
                        exchPath.child("status").value = ExchangeStatus.COMPLETED.name
                        exchPath.child("completedAt").value = System.currentTimeMillis()

                        Transaction.success(currentData)
                    } catch (_: Exception) {
                        Transaction.abort()
                    }
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    snapshot: DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resumeWithException(error.toException())
                    } else if (!committed) {
                        cont.resumeWithException(Exception("Transaction aborted"))
                    } else {
                        cont.resume(Unit)
                    }
                }
            })
        }
    }

    /**
     * Cancelar intercambio
     */
    suspend fun cancelExchange(exchangeId: String): kotlin.Result<Unit> {
        return try {
            exchangesRef.child(exchangeId)
                .child("status")
                .setValue(ExchangeStatus.CANCELLED.name)
                .await()
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}

/**
 * Modelo de datos para propuestas de intercambio
 */
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

/**
 * Estados posibles de un intercambio
 */
enum class ExchangeStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    EXPIRED
}