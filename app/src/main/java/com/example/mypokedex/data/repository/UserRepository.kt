package com.example.mypokedex.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestionar datos de usuario
 */
class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    /**
     * Guardar alias del usuario
     */
    suspend fun saveUserAlias(userId: String, alias: String): Boolean {
        return try {
            val userData = mapOf(
                "alias" to alias,
                "createdAt" to System.currentTimeMillis()
            )

            usersRef.child(userId).setValue(userData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtener alias del usuario
     */
    suspend fun getUserAlias(userId: String): String? {
        return try {
            val snapshot = usersRef.child(userId).child("alias").get().await()
            snapshot.getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verificar si un alias ya está en uso
     */
    suspend fun isAliasAvailable(alias: String): Boolean {
        return try {
            val snapshot = usersRef.orderByChild("alias").equalTo(alias).get().await()
            !snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }
}

data class UserData(
    val alias: String = "",
    val createdAt: Long = 0
)