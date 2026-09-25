package ar.edu.uade.fieldcheck.presentation.review

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.usecase.MissingItem

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data object NotFound : ReviewUiState
    data object Error : ReviewUiState

    data class Content(
        val inspection: Inspection,
        // null = todavía no intentó finalizar. Si hay faltantes, la lista se filtra a esos ítems.
        val missing: List<MissingItem>? = null,
        val showConfirmDialog: Boolean = false,
    ) : ReviewUiState
}

sealed interface ReviewEvent {
    data class Finished(val wasOffline: Boolean) : ReviewEvent
}
