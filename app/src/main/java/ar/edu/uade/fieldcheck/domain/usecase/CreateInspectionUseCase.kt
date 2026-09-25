package ar.edu.uade.fieldcheck.domain.usecase

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import java.util.UUID

// RF01: crea la inspección copiando los ítems de la plantilla, todos sin completar.
// El id se genera en el dispositivo para poder crearla sin conexión.
class CreateInspectionUseCase(
    private val repository: InspectionRepository,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(template: Template, siteName: String): String {
        val site = siteName.trim()
        require(site.isNotEmpty()) { "El sitio es obligatorio" }

        val inspection = Inspection(
            id = newId(),
            templateName = template.name,
            templateVersion = template.version,
            siteName = site,
            status = InspectionStatus.IN_PROGRESS,
            startedAt = now(),
            items = template.items
                .sortedBy { it.position }
                .mapIndexed { index, item ->
                    // Se copia el texto: si la plantilla cambia después, la inspección no se altera
                    ItemResult(id = index + 1L, position = item.position, text = item.text)
                },
        )
        repository.insertInspection(inspection)
        return inspection.id
    }
}
