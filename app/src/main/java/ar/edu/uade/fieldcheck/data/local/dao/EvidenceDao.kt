package ar.edu.uade.fieldcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ar.edu.uade.fieldcheck.data.local.entity.EvidenceEntity

// Las fotos se agregan en el paso de la cámara. Por ahora solo queda lo que necesita la sincronización.
@Dao
interface EvidenceDao {

    @Insert
    suspend fun insert(evidence: EvidenceEntity)

    // Fotos de una inspección que todavía no se subieron
    @Query(
        """
        SELECT evidence.* FROM evidence
        INNER JOIN item_results ON item_results.id = evidence.itemResultId
        WHERE item_results.inspectionId = :inspectionId AND evidence.syncStatus = 'PENDING'
        """
    )
    suspend fun getPendingForInspection(inspectionId: String): List<EvidenceEntity>

    @Query("UPDATE evidence SET syncStatus = 'SYNCED' WHERE clientUuid = :id")
    suspend fun markSynced(id: String)
}
