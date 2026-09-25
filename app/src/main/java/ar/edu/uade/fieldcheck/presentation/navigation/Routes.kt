package ar.edu.uade.fieldcheck.presentation.navigation

import kotlinx.serialization.Serializable

// Destinos de navegación con argumentos tipados (Navigation Compose 2.8+)
@Serializable
object HistoryRoute

@Serializable
object NewInspectionRoute

// itemId opcional: si viene de la revisión abre ese ítem; si no, el primero sin completar
@Serializable
data class ExecutionRoute(val inspectionId: String, val itemId: Long? = null)
