package ar.edu.uade.fieldcheck.domain.repository

import ar.edu.uade.fieldcheck.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    // Plantillas guardadas en el teléfono (caché)
    fun observeTemplates(): Flow<List<Template>>

    // Pide las plantillas al servidor y actualiza la caché. Lanza una excepción si falla.
    suspend fun refreshTemplates()
}
