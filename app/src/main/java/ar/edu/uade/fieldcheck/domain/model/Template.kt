package ar.edu.uade.fieldcheck.domain.model

// Plantilla de checklist que viene del servidor y se guarda como caché local
data class Template(
    val id: String,
    val name: String,
    val version: Int,
    val items: List<TemplateItem>,
    val downloadedAt: Long,
)

data class TemplateItem(
    val id: String,
    val text: String,
    val position: Int,
)
