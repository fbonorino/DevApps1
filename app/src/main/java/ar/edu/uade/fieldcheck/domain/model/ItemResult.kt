package ar.edu.uade.fieldcheck.domain.model

enum class ItemStatus { COMPLIES, NOT_COMPLIES, NOT_APPLICABLE }

// Resultado de un ítem dentro de una inspección. El texto se copia de la plantilla al crearla,
// así la inspección no cambia si después se modifica la plantilla.
data class ItemResult(
    val id: Long,
    val position: Int,
    val text: String,
    val status: ItemStatus? = null, // null = sin completar
    val note: String? = null,
    val evidence: List<Evidence> = emptyList(),
) {
    val isCompleted: Boolean get() = status != null

    // Regla del RF02: todo "No cumple" necesita una nota o una foto
    val needsEvidence: Boolean
        get() = status == ItemStatus.NOT_COMPLIES && note.isNullOrBlank() && evidence.isEmpty()
}

data class Evidence(
    val id: String,
    val filePath: String,
    val capturedAt: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
