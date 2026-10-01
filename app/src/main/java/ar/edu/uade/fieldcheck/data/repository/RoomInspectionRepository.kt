package ar.edu.uade.fieldcheck.data.repository

import ar.edu.uade.fieldcheck.data.local.dao.InspectionDao
import ar.edu.uade.fieldcheck.data.mapper.toDomain
import ar.edu.uade.fieldcheck.data.mapper.toEntity
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Todas las escrituras van a Room y dejan la inspección "Pendiente" de sincronizar.
// Ninguna depende de la red.
class RoomInspectionRepository(
    private val dao: InspectionDao,
    private val now: () -> Long = { System.currentTimeMillis() },
) : InspectionRepository {

    override fun observeInspections(): Flow<List<Inspection>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeInspection(id: String): Flow<Inspection?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun insertInspection(inspection: Inspection) {
        val time = now()
        // Los ids de los ítems los genera Room (id = 0): los que arma el caso de uso (1..n)
        // se repetirían entre inspecciones. Una inspección nueva todavía no tiene fotos.
        val items = inspection.items.map { it.toEntity(inspection.id, updatedAt = time).copy(id = 0) }
        dao.insertWithItems(inspection.toEntity(updatedAt = time), items)
    }

    override suspend fun updateItemStatus(inspectionId: String, itemId: Long, status: ItemStatus?) {
        dao.updateItemStatus(inspectionId, itemId, status, now())
    }

    override suspend fun updateItemNote(inspectionId: String, itemId: Long, note: String) {
        dao.updateItemNote(inspectionId, itemId, note, now())
    }

    override suspend fun finishInspection(inspectionId: String, finishedAt: Long) {
        dao.finish(inspectionId, finishedAt, now())
    }

    override suspend fun retrySync(inspectionId: String) {
        dao.markPending(inspectionId, now())
    }
}
