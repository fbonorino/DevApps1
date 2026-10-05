package ar.edu.uade.fieldcheck.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ar.edu.uade.fieldcheck.domain.model.SyncStatus

// La foto no se guarda en la base: solo la ruta del archivo en el almacenamiento interno.
// Tiene su propio syncStatus para poder reanudar la subida foto por foto.
@Entity(
    tableName = "evidence",
    foreignKeys = [
        ForeignKey(
            entity = ItemResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemResultId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemResultId")],
)
data class EvidenceEntity(
    @PrimaryKey val clientUuid: String, // generado en el dispositivo
    val itemResultId: Long,
    val filePath: String,
    val capturedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val syncStatus: SyncStatus,
)
