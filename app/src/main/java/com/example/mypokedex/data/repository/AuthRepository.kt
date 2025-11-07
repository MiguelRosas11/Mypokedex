package com.example.mypokedex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val userRepo: UserRepository, // Se inyecta (desde NavGraph)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    fun getCurrentUserId(): String? {
        return _currentUser.value?.uid
    }

    suspend fun getCurrentUserAlias(): String? {
        val uid = getCurrentUserId() ?: return null
        // !! CORREGIDO: Usar el nombre de función correcto
        return userRepo.getUserAlias(uid)
    }

    /**
     * !! LÓGICA CORREGIDA !!
     * Esta función ahora verifica si ya existe un usuario.
     * Si ya existe, solo actualiza su alias y lo devuelve.
     * Si no existe (instalación limpia), crea un nuevo usuario anónimo.
     * Esto asegura que solo tengas UN UID por instalación.
     */
    suspend fun signInWithAlias(alias: String): Resource<FirebaseUser> {
        // Validar alias
        if (alias.length !in 3..8) {
            return Resource.Error("El alias debe tener entre 3 y 8 caracteres")
        }

        return try {
            val existingUser = auth.currentUser
            val firebaseUser: FirebaseUser

            if (existingUser != null) {
                // Si ya estamos logueados, solo actualizar el alias y re-usar el usuario
                firebaseUser = existingUser
                // !! CORREGIDO: Usar el nombre de función correcto
                userRepo.saveUserAlias(firebaseUser.uid, alias)
                _currentUser.value = firebaseUser
                Resource.Success(firebaseUser)
            } else {
                // Si no estamos logueados (instalación limpia), crear nuevo usuario
                val authResult = auth.signInAnonymously().await()
                firebaseUser = authResult.user!!
                // !! CORREGIDO: Usar el nombre de función correcto
                userRepo.saveUserAlias(firebaseUser.uid, alias)
                _currentUser.value = firebaseUser
                Resource.Success(firebaseUser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Error desconocido de autenticación")
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}

// !! CORREGIDO: Añadir la clase Resource que faltaba
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}