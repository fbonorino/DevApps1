package ar.edu.uade.fieldcheck.domain.usecase

import ar.edu.uade.fieldcheck.domain.model.Evidence
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.model.TemplateItem
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionUseCasesTest {

    // Repositorio de prueba: solo anota lo que le piden los casos de uso
    private class FakeRepository : InspectionRepository {
        val inserted = mutableListOf<Inspection>()
        val finished = mutableListOf<Pair<String, Long>>()

        override fun observeInspections(): Flow<List<Inspection>> = emptyFlow()
        override fun observeInspection(id: String): Flow<Inspection?> = emptyFlow()
        override suspend fun insertInspection(inspection: Inspection) { inserted += inspection }
        override suspend fun finishInspection(inspectionId: String, finishedAt: Long) { finished += inspectionId to finishedAt }
        override suspend fun updateItemStatus(inspectionId: String, itemId: Long, status: ItemStatus?) = Unit
        override suspend fun updateItemNote(inspectionId: String, itemId: Long, note: String) = Unit
        override suspend fun retrySync(inspectionId: String) = Unit
    }

    private val repository = FakeRepository()

    private val template = Template(
        id = "tpl-obra",
        name = "Control semanal de obra",
        version = 3,
        // Desordenados a propósito: la inspección tiene que quedar ordenada por posición
        items = listOf(TemplateItem("b", "Segundo", 2), TemplateItem("a", "Primero", 1)),
        downloadedAt = 0L,
    )

    private fun inspectionWith(vararg items: ItemResult) = Inspection(
        id = "uuid-1",
        templateName = "Control semanal de obra",
        templateVersion = 3,
        siteName = "Obra",
        status = InspectionStatus.IN_PROGRESS,
        startedAt = 0L,
        items = items.toList(),
    )

    // ---- CreateInspectionUseCase ----

    @Test
    fun `create copies the template items in order, all without result`() = runTest {
        val create = CreateInspectionUseCase(repository, now = { 1_000L }, newId = { "uuid-nuevo" })

        val id = create(template, "  Obra Subsuelo 2  ")

        val saved = repository.inserted.single()
        assertEquals("uuid-nuevo", id)
        assertEquals("uuid-nuevo", saved.id)
        assertEquals("Obra Subsuelo 2", saved.siteName) // sin espacios de más
        assertEquals(InspectionStatus.IN_PROGRESS, saved.status)
        assertEquals(1_000L, saved.startedAt)
        assertEquals(listOf("Primero", "Segundo"), saved.items.map { it.text })
        assertTrue(saved.items.all { it.status == null })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create fails if the site is blank`() = runTest {
        CreateInspectionUseCase(repository)(template, "   ")
    }

    // ---- FinalizeInspectionUseCase ----

    @Test
    fun `finalize lists items without result and not-complies without evidence`() = runTest {
        val finalize = FinalizeInspectionUseCase(repository, now = { 9_000L })
        val inspection = inspectionWith(
            ItemResult(id = 1, position = 1, text = "Ok", status = ItemStatus.COMPLIES),
            ItemResult(id = 2, position = 2, text = "Sin completar"),
            ItemResult(id = 3, position = 3, text = "No cumple sin nota", status = ItemStatus.NOT_COMPLIES),
        )

        val result = finalize(inspection) as FinalizeResult.Missing

        assertEquals(
            listOf(2L to MissingReason.NOT_COMPLETED, 3L to MissingReason.NEEDS_EVIDENCE),
            result.items.map { it.item.id to it.reason },
        )
        assertTrue(repository.finished.isEmpty()) // no se finaliza si falta algo
    }

    @Test
    fun `finalize accepts not-complies with a note or a photo and saves the finish time`() = runTest {
        val finalize = FinalizeInspectionUseCase(repository, now = { 9_000L })
        val photo = Evidence(id = "foto", filePath = "files/evidence/foto.jpg", capturedAt = 0L)
        val inspection = inspectionWith(
            ItemResult(id = 1, position = 1, text = "Con nota", status = ItemStatus.NOT_COMPLIES, note = "Falta tapa"),
            ItemResult(id = 2, position = 2, text = "Con foto", status = ItemStatus.NOT_COMPLIES, evidence = listOf(photo)),
            ItemResult(id = 3, position = 3, text = "N/A", status = ItemStatus.NOT_APPLICABLE),
        )

        val result = finalize(inspection)

        assertEquals(FinalizeResult.Finished, result)
        assertEquals(listOf("uuid-1" to 9_000L), repository.finished)
    }

    @Test
    fun `a blank note does not count as evidence`() {
        val finalize = FinalizeInspectionUseCase(repository)
        val inspection = inspectionWith(
            ItemResult(id = 1, position = 1, text = "Nota vacía", status = ItemStatus.NOT_COMPLIES, note = "   "),
        )

        val missing = finalize.missingItems(inspection).single()

        assertEquals(MissingReason.NEEDS_EVIDENCE, missing.reason)
        assertNull(repository.finished.firstOrNull())
    }
}
