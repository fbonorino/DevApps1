package ar.edu.uade.fieldcheck.presentation.execution

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemResult

sealed interface ExecutionUiState {
    data object Loading : ExecutionUiState
    data object NotFound : ExecutionUiState
    data object Error : ExecutionUiState

    data class Content(
        val inspection: Inspection,
        val currentIndex: Int,
        val noteDraft: String, // lo que se está escribiendo; se guarda al dejar de escribir
    ) : ExecutionUiState {
        val currentItem: ItemResult get() = inspection.items[currentIndex]
        val isFirst: Boolean get() = currentIndex == 0
        val isLast: Boolean get() = currentIndex == inspection.items.lastIndex

        // El aviso usa la nota que se está escribiendo, así desaparece apenas se empieza a escribir
        val showEvidenceWarning: Boolean
            get() = currentItem.copy(note = noteDraft).needsEvidence
    }
}
