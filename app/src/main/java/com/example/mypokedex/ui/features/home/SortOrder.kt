package com.example.mypokedex.ui.features.home

/**
 * Enum que define los diferentes tipos de ordenamiento disponibles
 */
enum class SortOrder(val displayName: String) {
    ID_ASCENDING("ID (1→∞)"),
    ID_DESCENDING("ID (∞→1)"),
    NAME_ASCENDING("Nombre (A→Z)"),
    NAME_DESCENDING("Nombre (Z→A)")
}

/**
 * Función de extensión para obtener el siguiente tipo de ordenamiento
 * Permite ciclar entre los diferentes tipos de ordenamiento
 */
fun SortOrder.next(): SortOrder {
    return when (this) {
        SortOrder.ID_ASCENDING -> SortOrder.ID_DESCENDING
        SortOrder.ID_DESCENDING -> SortOrder.NAME_ASCENDING
        SortOrder.NAME_ASCENDING -> SortOrder.NAME_DESCENDING
        SortOrder.NAME_DESCENDING -> SortOrder.ID_ASCENDING
    }
}