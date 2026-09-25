package ar.edu.uade.fieldcheck.domain.model

enum class InspectionStatus { IN_PROGRESS, FINISHED }

enum class SyncStatus { PENDING, SYNCED, FAILED }

// Motivos de error de sincronización. La UI los traduce a un texto en lenguaje simple.
enum class SyncError { SERVER_UNREACHABLE, INVALID_API_KEY }

data class Inspection(
    val id: String, // clientUuid generado en el dispositivo
    val templateName: String,
    val templateVersion: Int,
    val siteName: String,
    val status: InspectionStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncError: SyncError? = null,
    val items: List<ItemResult>,
) {
    val completedCount: Int get() = items.count { it.isCompleted }
    val totalCount: Int get() = items.size

    fun count(status: ItemStatus): Int = items.count { it.status == status }

    // Para retomar: la inspección se abre en el primer ítem sin completar
    val firstPendingItem: ItemResult? get() = items.firstOrNull { !it.isCompleted }
}
