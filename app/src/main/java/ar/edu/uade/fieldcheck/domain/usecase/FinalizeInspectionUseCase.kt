package ar.edu.uade.fieldcheck.domain.usecase

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository

enum class MissingReason { NOT_COMPLETED, NEEDS_EVIDENCE }

data class MissingItem(val item: ItemResult, val reason: MissingReason)

sealed interface FinalizeResult {
    data object Finished : FinalizeResult
    data class Missing(val items: List<MissingItem>) : FinalizeResult
}

// Regla del RF02: para finalizar, todos los ítems tienen estado y todo "No cumple" tiene nota o foto
class FinalizeInspectionUseCase(
    private val repository: InspectionRepository,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    fun missingItems(inspection: Inspection): List<MissingItem> =
        inspection.items.mapNotNull { item ->
            when {
                !item.isCompleted -> MissingItem(item, MissingReason.NOT_COMPLETED)
                item.needsEvidence -> MissingItem(item, MissingReason.NEEDS_EVIDENCE)
                else -> null
            }
        }

    suspend operator fun invoke(inspection: Inspection): FinalizeResult {
        val missing = missingItems(inspection)
        if (missing.isNotEmpty()) return FinalizeResult.Missing(missing)
        // Queda "Pendiente" de sincronizar. Encolar el SyncWorker se agrega con WorkManager.
        repository.finishInspection(inspection.id, now())
        return FinalizeResult.Finished
    }
}
