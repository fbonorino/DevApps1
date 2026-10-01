package ar.edu.uade.fieldcheck.data.mapper

import ar.edu.uade.fieldcheck.data.local.entity.EvidenceEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionWithItems
import ar.edu.uade.fieldcheck.data.local.entity.ItemResultEntity
import ar.edu.uade.fieldcheck.data.local.entity.ItemWithEvidence
import ar.edu.uade.fieldcheck.domain.model.Evidence
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.model.SyncError
import ar.edu.uade.fieldcheck.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class InspectionMapperTest {

    private val inspectionEntity = InspectionEntity(
        clientUuid = "uuid-1",
        templateName = "Control semanal de obra",
        templateVersion = 3,
        siteName = "Obra Subsuelo 2",
        status = InspectionStatus.FINISHED,
        startedAt = 1_000L,
        finishedAt = 2_000L,
        syncStatus = SyncStatus.FAILED,
        lastSyncError = SyncError.INVALID_API_KEY,
        updatedAt = 2_500L,
    )

    private fun item(id: Long, position: Int, evidence: List<EvidenceEntity> = emptyList()) = ItemWithEvidence(
        item = ItemResultEntity(
            id = id,
            inspectionId = "uuid-1",
            itemText = "Ítem $position",
            position = position,
            result = if (position == 1) ItemStatus.NOT_COMPLIES else null,
            note = if (position == 1) "Falta tapa" else null,
            updatedAt = 1_500L,
        ),
        evidence = evidence,
    )

    private fun evidence(id: String, capturedAt: Long) = EvidenceEntity(
        clientUuid = id,
        itemResultId = 10L,
        filePath = "files/evidence/$id.jpg",
        capturedAt = capturedAt,
        latitude = -34.6,
        longitude = null,
        syncStatus = SyncStatus.PENDING,
    )

    @Test
    fun `entity to domain copies every field`() {
        val domain = InspectionWithItems(inspectionEntity, listOf(item(10L, 1))).toDomain()

        assertEquals("uuid-1", domain.id)
        assertEquals("Control semanal de obra", domain.templateName)
        assertEquals(3, domain.templateVersion)
        assertEquals("Obra Subsuelo 2", domain.siteName)
        assertEquals(InspectionStatus.FINISHED, domain.status)
        assertEquals(1_000L, domain.startedAt)
        assertEquals(2_000L, domain.finishedAt)
        assertEquals(SyncStatus.FAILED, domain.syncStatus)
        assertEquals(SyncError.INVALID_API_KEY, domain.lastSyncError)

        val item = domain.items.single()
        assertEquals(10L, item.id)
        assertEquals("Ítem 1", item.text)
        assertEquals(ItemStatus.NOT_COMPLIES, item.status)
        assertEquals("Falta tapa", item.note)
    }

    @Test
    fun `items come out ordered by position and evidence by capture time`() {
        val unordered = InspectionWithItems(
            inspectionEntity,
            listOf(
                item(12L, 3),
                item(10L, 1, evidence = listOf(evidence("b", 20L), evidence("a", 10L))),
                item(11L, 2),
            ),
        )

        val domain = unordered.toDomain()

        assertEquals(listOf(1, 2, 3), domain.items.map { it.position })
        assertEquals(listOf("a", "b"), domain.items.first().evidence.map { it.id })
    }

    @Test
    fun `domain to entity and back keeps the same inspection`() {
        val original = Inspection(
            id = "uuid-2",
            templateName = "Depósito",
            templateVersion = 2,
            siteName = "Nave 3",
            status = InspectionStatus.IN_PROGRESS,
            startedAt = 5_000L,
            items = listOf(
                ItemResult(id = 1L, position = 1, text = "Salidas despejadas", status = ItemStatus.COMPLIES),
                ItemResult(id = 2L, position = 2, text = "Iluminación de emergencia"),
            ),
        )

        val entity = original.toEntity(updatedAt = 6_000L)
        val items = original.items.map { ItemWithEvidence(it.toEntity(original.id, updatedAt = 6_000L), emptyList()) }
        val back = InspectionWithItems(entity, items).toDomain()

        assertEquals(original, back)
        assertEquals(6_000L, entity.updatedAt)
        assertEquals("uuid-2", items.first().item.inspectionId)
    }

    @Test
    fun `evidence to entity starts pending and keeps the file path`() {
        val evidence = Evidence(id = "foto-1", filePath = "files/evidence/foto-1.jpg", capturedAt = 7L, latitude = 1.0, longitude = 2.0)

        val entity = evidence.toEntity(itemResultId = 10L)

        assertEquals(SyncStatus.PENDING, entity.syncStatus)
        assertEquals(10L, entity.itemResultId)
        assertEquals(evidence, entity.toDomain())
    }
}
