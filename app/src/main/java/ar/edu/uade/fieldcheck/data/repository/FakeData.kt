package ar.edu.uade.fieldcheck.data.repository

import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.model.SyncError
import ar.edu.uade.fieldcheck.domain.model.SyncStatus
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.model.TemplateItem

// Datos de ejemplo para desarrollar la UI antes de tener Room y la API
object FakeData {

    private const val HOUR = 60 * 60 * 1000L
    private const val DAY = 24 * HOUR

    private val obraItems = listOf(
        "El vallado perimetral está completo y en buen estado",
        "La cartelería de obra y de seguridad está visible",
        "Los trabajadores usan casco y calzado de seguridad",
        "Los accesos y circulaciones están libres de obstáculos",
        "Las escaleras y andamios están firmes y con barandas",
        "Los materiales están acopiados en forma ordenada",
        "Los matafuegos están señalizados y con la carga vigente",
        "El botiquín de primeros auxilios está completo",
        "Los residuos se separan en los contenedores indicados",
        "Los tableros eléctricos tienen tapa y señalización",
    )

    private val depositoItems = listOf(
        "Las salidas de emergencia están despejadas y señalizadas",
        "La iluminación de emergencia funciona",
        "Las estanterías están ancladas y sin sobrecarga",
        "Los pasillos tienen el ancho mínimo libre",
        "Hay registro actualizado de control de plagas",
        "Los productos químicos están rotulados y separados",
    )

    private val mantenimientoItems = listOf(
        "No hay filtraciones ni manchas de humedad en techos",
        "Los ascensores tienen la habilitación vigente",
        "Las bombas de agua funcionan sin ruidos extraños",
        "Las luminarias de espacios comunes funcionan",
        "Las puertas cortafuego cierran solas",
    )

    fun templates(downloadedAt: Long): List<Template> = listOf(
        template("tpl-obra", "Control semanal de obra", 3, obraItems, downloadedAt),
        template("tpl-deposito", "Seguridad e higiene en depósito", 2, depositoItems, downloadedAt),
        template("tpl-mant", "Recorrida de mantenimiento edilicio", 1, mantenimientoItems, downloadedAt),
    )

    fun inspections(now: Long = System.currentTimeMillis()): List<Inspection> = listOf(
        // En curso: 4 de 10 completados
        Inspection(
            id = "a1f3c2d4-0001",
            templateName = "Control semanal de obra",
            templateVersion = 3,
            siteName = "Obra Av. Siempreviva 742 — Subsuelo 2",
            status = InspectionStatus.IN_PROGRESS,
            startedAt = now - HOUR,
            items = results(obraItems) { index ->
                when (index) {
                    0, 1, 3 -> ItemStatus.COMPLIES
                    2 -> ItemStatus.NOT_APPLICABLE
                    else -> null
                }
            },
        ),
        // Finalizada, pendiente de subir
        Inspection(
            id = "a1f3c2d4-0002",
            templateName = "Seguridad e higiene en depósito",
            templateVersion = 2,
            siteName = "Depósito Barracas — Nave 3",
            status = InspectionStatus.FINISHED,
            startedAt = now - 5 * HOUR,
            finishedAt = now - 4 * HOUR,
            syncStatus = SyncStatus.PENDING,
            items = results(depositoItems, notes = mapOf(3 to "Pallets en el pasillo central.")) { index ->
                if (index == 3) ItemStatus.NOT_COMPLIES else ItemStatus.COMPLIES
            },
        ),
        // Finalizada con error de sincronización
        Inspection(
            id = "a1f3c2d4-0003",
            templateName = "Recorrida de mantenimiento edilicio",
            templateVersion = 1,
            siteName = "Edificio Corrientes 1234",
            status = InspectionStatus.FINISHED,
            startedAt = now - DAY - 2 * HOUR,
            finishedAt = now - DAY - HOUR,
            syncStatus = SyncStatus.FAILED,
            lastSyncError = SyncError.INVALID_API_KEY,
            items = results(mantenimientoItems) { index ->
                if (index == 1) ItemStatus.NOT_APPLICABLE else ItemStatus.COMPLIES
            },
        ),
        // Finalizada y sincronizada
        Inspection(
            id = "a1f3c2d4-0004",
            templateName = "Control semanal de obra",
            templateVersion = 3,
            siteName = "Obra Av. Siempreviva 742 — Planta baja",
            status = InspectionStatus.FINISHED,
            startedAt = now - 3 * DAY,
            finishedAt = now - 3 * DAY + HOUR,
            syncStatus = SyncStatus.SYNCED,
            items = results(
                obraItems,
                notes = mapOf(
                    6 to "Falta señalización en el matafuego del palier.",
                    9 to "Tablero TS-3 sin tapa.",
                ),
            ) { index ->
                when (index) {
                    6, 9 -> ItemStatus.NOT_COMPLIES
                    7 -> ItemStatus.NOT_APPLICABLE
                    else -> ItemStatus.COMPLIES
                }
            },
        ),
    )

    private fun template(id: String, name: String, version: Int, texts: List<String>, downloadedAt: Long) =
        Template(
            id = id,
            name = name,
            version = version,
            items = texts.mapIndexed { index, text -> TemplateItem("$id-${index + 1}", text, index + 1) },
            downloadedAt = downloadedAt,
        )

    private fun results(
        texts: List<String>,
        notes: Map<Int, String> = emptyMap(),
        statusFor: (Int) -> ItemStatus?,
    ): List<ItemResult> = texts.mapIndexed { index, text ->
        ItemResult(
            id = index + 1L,
            position = index + 1,
            text = text,
            status = statusFor(index),
            note = notes[index],
        )
    }
}
