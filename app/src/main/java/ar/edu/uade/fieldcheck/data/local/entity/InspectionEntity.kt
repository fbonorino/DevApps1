package ar.edu.uade.fieldcheck.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.SyncError
import ar.edu.uade.fieldcheck.domain.model.SyncStatus

// No tiene FK a la plantilla: la caché de plantillas se puede borrar y la inspección
// tiene que seguir existiendo. Por eso se copian el nombre y la versión.
@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey val clientUuid: String, // generado en el dispositivo
    val templateName: String,
    val templateVersion: Int,
    val siteName: String,
    val status: InspectionStatus,
    val startedAt: Long,
    val finishedAt: Long?,
    val syncStatus: SyncStatus,
    val lastSyncError: SyncError?,
    val updatedAt: Long,
)

// Inspección completa: sus ítems y, dentro de cada ítem, sus fotos
data class InspectionWithItems(
    @Embedded val inspection: InspectionEntity,
    @Relation(entity = ItemResultEntity::class, parentColumn = "clientUuid", entityColumn = "inspectionId")
    val items: List<ItemWithEvidence>,
)
