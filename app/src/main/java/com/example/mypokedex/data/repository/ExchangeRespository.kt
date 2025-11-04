package com.example.mypokedex.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Repository para gestionar intercambios de Pokémon
 *
 * Flujo del intercambio:
 * 1. Usuario A crea propuesta con su Pokémon
 * 2. Usuario B escanea QR y ve la propuesta
 * 3. Usuario B selecciona su Pokémon y acepta
 * 4. Se ejecuta transacción atómica
 * 5. Ambos usuarios reciben notificación
 */
class ExchangeRepository {
    private val database = FirebaseDatabase.getInstance()
    private val exchangesRef = database.getReference("exchanges")
    private val favoritesRef = database.getReference("favorites")

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
            val exchangeId = exchangesRef.push().key ?: return kotlin.Result.failure(
                Exception("Error al generar ID")
            )

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
        } catch (e: Exception) {
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

        awaitClose {
            exchangesRef.child(exchangeId).removeEventListener(listener)
        }
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
            // Obtener propuesta
            val proposal = getExchangeProposal(exchangeId)
                ?: return kotlin.Result.failure(Exception("Propuesta no encontrada"))

            // Validar que la propuesta está pendiente
            if (proposal.status != ExchangeStatus.PENDING) {
                return kotlin.Result.failure(Exception("Intercambio ya procesado"))
            }

            // Validar timeout (90 segundos)
            val now = System.currentTimeMillis()
            if (now - proposal.createdAt > 90_000) {
                // Marcar como expirado
                exchangesRef.child(exchangeId)
                    .child("status")
                    .setValue(ExchangeStatus.EXPIRED.name)
                    .await()

                return kotlin.Result.failure(Exception("Intercambio expirado"))
            }

            // Ejecutar transacción atómica
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
     * Ejecutar transacción atómica de intercambio
     * Esta operación es ACID:
     * - Atomic: Se completa totalmente o falla totalmente
     * - Consistent: Los datos quedan en estado consistente
     * - Isolated: No hay interferencia de otras operaciones
     * - Durable: Los cambios son permanentes
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
            val ref = database.reference
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    try {
                        val pokemonAData = currentData
                            .child("favorites")
                            .child(userAId)
                            .child(pokemonAId.toString())
                        val pokemonBData = currentData
                            .child("favorites")
                            .child(userBId)
                            .child(pokemonBId.toString())

                        val pokemonAExists = pokemonAData.value != null
                        val pokemonBExists = pokemonBData.value != null

                        if (!pokemonAExists || !pokemonBExists) {
                            return Transaction.abort()
                        }

                        // Eliminar Pokémon de sus dueños originales
                        currentData.child("favorites").child(userAId)
                            .child(pokemonAId.toString()).value = null
                        currentData.child("favorites").child(userBId)
                            .child(pokemonBId.toString()).value = null

                        // Agregar Pokémon a nuevos dueños
                        currentData.child("favorites").child(userAId)
                            .child(pokemonBId.toString()).value = mapOf(
                            "id" to pokemonBId,
                            "name" to pokemonBName,
                            "imageUrl" to pokemonBImageUrl,
                            "addedAt" to System.currentTimeMillis()
                        )
                        currentData.child("favorites").child(userBId)
                            .child(pokemonAId.toString()).value = mapOf(
                            "id" to pokemonAId,
                            "name" to pokemonAName,
                            "imageUrl" to pokemonAImageUrl,
                            "addedAt" to System.currentTimeMillis()
                        )

                        // Actualizar estado del intercambio
                        val exchPath = currentData.child("exchanges").child(exchangeId)
                        exchPath.child("userBId").value = userBId
                        exchPath.child("userBAlias").value = userBAlias
                        exchPath.child("pokemonBId").value = pokemonBId
                        exchPath.child("pokemonBName").value = pokemonBName
                        exchPath.child("pokemonBImageUrl").value = pokemonBImageUrl
                        exchPath.child("status").value = ExchangeStatus.COMPLETED.name
                        exchPath.child("completedAt").value = System.currentTimeMillis()

                        return Transaction.success(currentData)
                    } catch (e: Exception) {
                        return Transaction.abort()
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
 * Modelo de propuesta de intercambio
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
 * Estados del intercambio
 */
enum class ExchangeStatus {
    PENDING,    // Esperando aceptación
    COMPLETED,  // Completado exitosamente
    CANCELLED,  // Cancelado por algún usuario
    EXPIRED     // Expiró el tiempo límite (90s)
}