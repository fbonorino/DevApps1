package ar.edu.uade.fieldcheck.domain.repository

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

interface InspectionRepository {
    fun observeInspections(): Flow<List<Inspection>>

    // Emite null si la inspección no existe
    fun observeInspection(id: String): Flow<Inspection?>

    suspend fun insertInspection(inspection: Inspection)

    suspend fun updateItemStatus(inspectionId: String, itemId: Long, status: ItemStatus?)

    suspend fun updateItemNote(inspectionId: String, itemId: Long, note: String)

    suspend fun finishInspection(inspectionId: String, finishedAt: Long)

    suspend fun retrySync(inspectionId: String)
}
