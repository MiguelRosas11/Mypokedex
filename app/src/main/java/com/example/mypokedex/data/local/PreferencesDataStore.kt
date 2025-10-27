package com.example.mypokedex.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey  
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para obtener el DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pokemon_preferences")

class PreferencesDataStore(private val context: Context) {

    companion object {
        // ← CAMBIO AQUÍ: usar stringPreferencesKey en lugar de stringPreference
        private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        private val SORT_DIRECTION_KEY = stringPreferencesKey("sort_direction")

        // Valores por defecto
        const val DEFAULT_SORT_ORDER = "ID" // ID o NAME
        const val DEFAULT_SORT_DIRECTION = "ASC" // ASC o DESC
    }

    suspend fun saveSortOrder(sortBy: String, direction: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortBy
            preferences[SORT_DIRECTION_KEY] = direction
        }
    }

    val sortOrderFlow: Flow<Pair<String, String>> = context.dataStore.data.map { preferences ->
        val sortBy = preferences[SORT_ORDER_KEY] ?: DEFAULT_SORT_ORDER
        val direction = preferences[SORT_DIRECTION_KEY] ?: DEFAULT_SORT_DIRECTION
        Pair(sortBy, direction)
    }

    suspend fun getSortOrder(): Pair<String, String> {
        val preferences = context.dataStore.data.map { preferences ->
            val sortBy = preferences[SORT_ORDER_KEY] ?: DEFAULT_SORT_ORDER
            val direction = preferences[SORT_DIRECTION_KEY] ?: DEFAULT_SORT_DIRECTION
            Pair(sortBy, direction)
        }
        var result = Pair(DEFAULT_SORT_ORDER, DEFAULT_SORT_DIRECTION)
        preferences.collect { result = it }
        return result
    }
}