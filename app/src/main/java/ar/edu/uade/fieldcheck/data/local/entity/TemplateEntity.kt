package ar.edu.uade.fieldcheck.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Caché de las plantillas que vienen del servidor. Se puede reemplazar entera.
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String, // id del servidor
    val name: String,
    val version: Int,
    val downloadedAt: Long, // para mostrar la antigüedad de la caché
)

@Entity(
    tableName = "template_items",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId")],
)
data class TemplateItemEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val text: String,
    val position: Int,
)

// Plantilla con sus ítems, para leerla en una sola consulta
data class TemplateWithItems(
    @Embedded val template: TemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val items: List<TemplateItemEntity>,
)
