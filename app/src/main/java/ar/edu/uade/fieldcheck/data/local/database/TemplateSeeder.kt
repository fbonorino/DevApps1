package ar.edu.uade.fieldcheck.data.local.database

import ar.edu.uade.fieldcheck.data.local.dao.TemplateDao
import ar.edu.uade.fieldcheck.data.mapper.toEntity
import ar.edu.uade.fieldcheck.data.repository.FakeData

// Hasta tener la API, la primera vez que se abre la app se cargan las plantillas de ejemplo
// para que se pueda crear una inspección. Si ya hay plantillas guardadas no hace nada.
suspend fun seedTemplatesIfEmpty(dao: TemplateDao, now: Long = System.currentTimeMillis()) {
    if (dao.count() > 0) return
    val templates = FakeData.templates(downloadedAt = now)
    dao.replaceAll(
        templates = templates.map { it.toEntity() },
        items = templates.flatMap { template -> template.items.map { it.toEntity(template.id) } },
    )
}
