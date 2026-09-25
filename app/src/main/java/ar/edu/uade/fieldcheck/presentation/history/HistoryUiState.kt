package ar.edu.uade.fieldcheck.presentation.history

import ar.edu.uade.fieldcheck.domain.model.Inspection

// "Sin conexión" no está acá: no reemplaza al contenido, se muestra encima como banner
sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data object Error : HistoryUiState
    data class Content(
        val inProgress: List<Inspection>,
        val finished: List<Inspection>,
    ) : HistoryUiState
}
