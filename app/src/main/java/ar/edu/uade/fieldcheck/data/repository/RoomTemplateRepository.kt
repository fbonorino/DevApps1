package ar.edu.uade.fieldcheck.data.repository

import ar.edu.uade.fieldcheck.data.local.dao.TemplateDao
import ar.edu.uade.fieldcheck.data.mapper.toDomain
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTemplateRepository(
    private val dao: TemplateDao,
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<Template>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    // Todavía no hay API: la descarga se agrega con Retrofit (paso 2).
    // Mientras tanto no hace nada y la pantalla sigue usando lo que hay en la caché.
    override suspend fun refreshTemplates() = Unit
}
