package com.example.mypokedex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestionar autenticación con Firebase
 * Usa autenticación anónima para velocidad en la competencia
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Flow que emite el usuario actual
     */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        // Emitir estado inicial
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Autenticación anónima (la mas quick para la compe)
     * Genera un UID único que podemos personalizar
     */
    suspend fun signInAnonymously(): Resource<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                Resource.Success(user) //
            } else {
                Resource.Error("Error al autenticar")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Autenticacion con alias personalizado
     * El usuario ingresa un alias corto (4-8 caracteres)
     * Se crea una cuenta anonima y se usa el alias como identificador
     */
    suspend fun signInWithAlias(alias: String): Resource<String> {
        return try {
            // Validar alias
            if (alias.length !in 3..8) {
                return Resource.Error("El alias debe tener entre 3 y 8 caracteres")
            }

            // Crear cuenta anónima
            val authResult = auth.signInAnonymously().await()
            val uid = authResult.user?.uid ?: return Resource.Error("Error al autenticar")
            // El alias se guardará en Firebase Database junto con el UID
            Resource.Success(uid)
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Cerrar sesión
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Obtener UID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Verificar si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}