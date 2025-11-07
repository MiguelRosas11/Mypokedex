package com.example.mypokedex.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestionar favoritos en Firebase Realtime Database
 * Estructura:
 * favorites/
 *   {userId}/
 *     {pokemonId}: { id, name, imageUrl, addedAt }
 */
class FavoritesRepository {
    private val database = FirebaseDatabase.getInstance()
    private val favoritesRef = database.getReference("favorites")

    /**
     * Obtener favoritos de un usuario en tiempo real
     */
    fun getUserFavorites(userId: String): Flow<List<FavoritePokemon>> = callbackFlow {
        val userFavoritesRef = favoritesRef.child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = snapshot.children.mapNotNull { child ->
                    child.getValue(FavoritePokemon::class.java)
                }
                trySend(favorites)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userFavoritesRef.addValueEventListener(listener)

        awaitClose {
            userFavoritesRef.removeEventListener(listener)
        }
    }

    /**
     * Agregar Pokémon a favoritos
     */
    suspend fun addFavorite(
        userId: String,
        pokemonId: Int,
        pokemonName: String,
        imageUrl: String
    ): Boolean {
        return try {
            val favorite = FavoritePokemon(
                id = pokemonId,
                name = pokemonName,
                imageUrl = imageUrl,
                addedAt = System.currentTimeMillis()
            )

            favoritesRef
                .child(userId)
                .child(pokemonId.toString())
                .setValue(favorite)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Eliminar Pokémon de favoritos
     */
    suspend fun removeFavorite(userId: String, pokemonId: Int): Boolean {
        return try {
            favoritesRef
                .child(userId)
                .child(pokemonId.toString())
                .removeValue()
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verificar si un Pokémon es favorito
     */
    suspend fun isFavorite(userId: String, pokemonId: Int): Boolean {
        return try {
            val snapshot = favoritesRef
                .child(userId)
                .child(pokemonId.toString())
                .get()
                .await()

            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener todos los favoritos de un usuario (una vez)
     */
    suspend fun getFavoritesOnce(userId: String): List<FavoritePokemon> {
        return try {
            val snapshot = favoritesRef
                .child(userId)
                .get()
                .await()

            snapshot.children.mapNotNull { child ->
                child.getValue(FavoritePokemon::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Modelo de Pokémon favorito
 */
data class FavoritePokemon(
    val id: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
    val addedAt: Long = 0
)