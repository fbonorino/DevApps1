package ar.edu.uade.fieldcheck.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import ar.edu.uade.fieldcheck.domain.model.ItemStatus

@Entity(
    tableName = "item_results",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["clientUuid"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("inspectionId")],
)
data class ItemResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectionId: String,
    val itemText: String, // copia del texto de la plantilla
    val position: Int,
    val result: ItemStatus?, // null = sin completar
    val note: String?,
    val updatedAt: Long,
)

data class ItemWithEvidence(
    @Embedded val item: ItemResultEntity,
    @Relation(parentColumn = "id", entityColumn = "itemResultId")
    val evidence: List<EvidenceEntity>,
)
