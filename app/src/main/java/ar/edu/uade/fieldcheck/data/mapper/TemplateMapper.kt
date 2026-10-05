package ar.edu.uade.fieldcheck.data.mapper

import ar.edu.uade.fieldcheck.data.local.entity.TemplateEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateItemEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateWithItems
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.model.TemplateItem

fun TemplateWithItems.toDomain(): Template = Template(
    id = template.id,
    name = template.name,
    version = template.version,
    // Room no garantiza el orden de las relaciones, así que se ordena acá
    items = items.sortedBy { it.position }.map { it.toDomain() },
    downloadedAt = template.downloadedAt,
)

fun TemplateItemEntity.toDomain(): TemplateItem = TemplateItem(id = id, text = text, position = position)

fun Template.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    version = version,
    downloadedAt = downloadedAt,
)

fun TemplateItem.toEntity(templateId: String): TemplateItemEntity = TemplateItemEntity(
    id = id,
    templateId = templateId,
    text = text,
    position = position,
)
