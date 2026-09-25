package ar.edu.uade.fieldcheck.presentation.newinspection

import ar.edu.uade.fieldcheck.domain.model.Template

sealed interface NewInspectionUiState {
    // Primera descarga: no hay nada guardado todavía
    data object Loading : NewInspectionUiState

    data class Content(
        val templates: List<Template>,
        val lastUpdatedAt: Long,
        val isRefreshing: Boolean, // hay lista guardada y se está actualizando en segundo plano
    ) : NewInspectionUiState

    // Sin plantillas: porque falta conexión la primera vez, o porque el servidor no tiene ninguna
    data class Empty(val needsConnection: Boolean) : NewInspectionUiState

    // Sin caché y falló el servidor
    data object Error : NewInspectionUiState
}

// Cosas que pasan una sola vez (navegar, mostrar un Snackbar), no son parte del estado
sealed interface NewInspectionEvent {
    data class InspectionCreated(val inspectionId: String) : NewInspectionEvent
    data object RefreshFailed : NewInspectionEvent
}
