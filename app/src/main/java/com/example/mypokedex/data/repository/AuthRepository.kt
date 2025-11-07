package com.example.mypokedex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestionar autenticación con Firebase
 */
class AuthRepository(
    private val userRepository: UserRepository = UserRepository()
) {
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
     * Autenticación anónima con alias
     */
    suspend fun signInAnonymously(): Resource<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Error al autenticar")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Autenticación con alias personalizado
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

            // Guardar alias en Firebase
            val saved = userRepository.saveUserAlias(uid, alias)
            if (!saved) {
                return Resource.Error("Error al guardar alias")
            }

            Resource.Success(uid)
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Obtener alias del usuario actual
     */
    suspend fun getCurrentUserAlias(): String? {
        val uid = getCurrentUserId() ?: return null
        return userRepository.getUserAlias(uid)
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