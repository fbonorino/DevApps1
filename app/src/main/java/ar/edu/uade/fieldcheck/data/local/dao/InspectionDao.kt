package ar.edu.uade.fieldcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ar.edu.uade.fieldcheck.data.local.entity.InspectionEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionWithItems
import ar.edu.uade.fieldcheck.data.local.entity.ItemResultEntity
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.model.SyncError
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InspectionDao {

    // ---- Lecturas que observan las pantallas ----

    // Historial: las más recientes primero
    @Transaction
    @Query("SELECT * FROM inspections ORDER BY startedAt DESC")
    abstract fun observeAll(): Flow<List<InspectionWithItems>>

    // Emite null si no existe
    @Transaction
    @Query("SELECT * FROM inspections WHERE clientUuid = :id")
    abstract fun observeById(id: String): Flow<InspectionWithItems?>

    // ---- Escrituras ----

    @Transaction
    open suspend fun insertWithItems(inspection: InspectionEntity, items: List<ItemResultEntity>) {
        insertInspection(inspection)
        insertItems(items)
    }

    // Cada cambio en un ítem deja la inspección "Pendiente" de sincronizar
    @Transaction
    open suspend fun updateItemStatus(inspectionId: String, itemId: Long, status: ItemStatus?, now: Long) {
        setItemResult(inspectionId, itemId, status, now)
        markPending(inspectionId, now)
    }

    @Transaction
    open suspend fun updateItemNote(inspectionId: String, itemId: Long, note: String, now: Long) {
        setItemNote(inspectionId, itemId, note, now)
        markPending(inspectionId, now)
    }

    @Query(
        """
        UPDATE inspections
        SET status = 'FINISHED', finishedAt = :finishedAt, syncStatus = 'PENDING',
            lastSyncError = NULL, updatedAt = :now
        WHERE clientUuid = :id
        """
    )
    abstract suspend fun finish(id: String, finishedAt: Long, now: Long)

    // También la usa "Reintentar": vuelve a dejarla en la cola
    @Query("UPDATE inspections SET syncStatus = 'PENDING', lastSyncError = NULL, updatedAt = :now WHERE clientUuid = :id")
    abstract suspend fun markPending(id: String, now: Long)

    @Insert
    protected abstract suspend fun insertInspection(inspection: InspectionEntity)

    @Insert
    protected abstract suspend fun insertItems(items: List<ItemResultEntity>)

    @Query("UPDATE item_results SET result = :status, updatedAt = :now WHERE id = :itemId AND inspectionId = :inspectionId")
    protected abstract suspend fun setItemResult(inspectionId: String, itemId: Long, status: ItemStatus?, now: Long)

    @Query("UPDATE item_results SET note = :note, updatedAt = :now WHERE id = :itemId AND inspectionId = :inspectionId")
    protected abstract suspend fun setItemNote(inspectionId: String, itemId: Long, note: String, now: Long)

    // ---- Para la sincronización (paso 3, la usa el SyncWorker) ----

    // Solo las finalizadas: las que están en curso todavía se pueden modificar.
    // Las FAILED no entran: esperan a que el usuario toque "Reintentar".
    @Transaction
    @Query("SELECT * FROM inspections WHERE status = 'FINISHED' AND syncStatus = 'PENDING'")
    abstract suspend fun getPendingInspections(): List<InspectionWithItems>

    @Query("UPDATE inspections SET syncStatus = 'SYNCED', lastSyncError = NULL WHERE clientUuid = :id")
    abstract suspend fun markSynced(id: String)

    @Query("UPDATE inspections SET syncStatus = 'FAILED', lastSyncError = :error WHERE clientUuid = :id")
    abstract suspend fun markSyncFailed(id: String, error: SyncError)
}
