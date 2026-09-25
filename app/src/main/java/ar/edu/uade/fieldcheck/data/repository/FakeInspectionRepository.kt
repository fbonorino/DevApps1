package ar.edu.uade.fieldcheck.data.repository

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.model.SyncStatus
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

// Repositorio en memoria. Se reemplaza por la versión con Room sin tocar ViewModels ni pantallas.
class FakeInspectionRepository(
    private val scenario: FakeScenario,
    private val loadingDelayMs: Long = 800,
) : InspectionRepository {

    private val inspections = MutableStateFlow(
        if (scenario == FakeScenario.CONTENT) FakeData.inspections() else emptyList()
    )

    override fun observeInspections(): Flow<List<Inspection>> = flow {
        delay(loadingDelayMs) // simula la lectura de la base para ver el estado de carga
        if (scenario == FakeScenario.ERROR) throw IOException("Error simulado al leer la base local")
        emitAll(inspections.map { list -> list.sortedByDescending { it.startedAt } })
    }

    override fun observeInspection(id: String): Flow<Inspection?> = flow {
        delay(loadingDelayMs / 2)
        if (scenario == FakeScenario.ERROR) throw IOException("Error simulado al leer la base local")
        emitAll(inspections.map { list -> list.find { it.id == id } })
    }

    override suspend fun insertInspection(inspection: Inspection) {
        inspections.update { it + inspection }
    }

    override suspend fun updateItemStatus(inspectionId: String, itemId: Long, status: ItemStatus?) {
        updateItem(inspectionId, itemId) { it.copy(status = status) }
    }

    override suspend fun updateItemNote(inspectionId: String, itemId: Long, note: String) {
        updateItem(inspectionId, itemId) { it.copy(note = note) }
    }

    override suspend fun finishInspection(inspectionId: String, finishedAt: Long) {
        updateInspection(inspectionId) {
            it.copy(
                status = InspectionStatus.FINISHED,
                finishedAt = finishedAt,
                syncStatus = SyncStatus.PENDING,
            )
        }
    }

    override suspend fun retrySync(inspectionId: String) {
        updateInspection(inspectionId) { it.copy(syncStatus = SyncStatus.PENDING, lastSyncError = null) }
    }

    private fun updateInspection(id: String, change: (Inspection) -> Inspection) {
        inspections.update { list -> list.map { if (it.id == id) change(it) else it } }
    }

    private fun updateItem(inspectionId: String, itemId: Long, change: (ItemResult) -> ItemResult) {
        updateInspection(inspectionId) { inspection ->
            inspection.copy(items = inspection.items.map { if (it.id == itemId) change(it) else it })
        }
    }
}
