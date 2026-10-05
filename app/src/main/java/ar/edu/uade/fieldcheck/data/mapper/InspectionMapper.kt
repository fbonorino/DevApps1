package ar.edu.uade.fieldcheck.data.mapper

import ar.edu.uade.fieldcheck.data.local.entity.EvidenceEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionWithItems
import ar.edu.uade.fieldcheck.data.local.entity.ItemResultEntity
import ar.edu.uade.fieldcheck.data.local.entity.ItemWithEvidence
import ar.edu.uade.fieldcheck.domain.model.Evidence
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.SyncStatus

// ---- Entidad -> dominio ----

fun InspectionWithItems.toDomain(): Inspection = Inspection(
    id = inspection.clientUuid,
    templateName = inspection.templateName,
    templateVersion = inspection.templateVersion,
    siteName = inspection.siteName,
    status = inspection.status,
    startedAt = inspection.startedAt,
    finishedAt = inspection.finishedAt,
    syncStatus = inspection.syncStatus,
    lastSyncError = inspection.lastSyncError,
    // Room no garantiza el orden de las relaciones, así que se ordena acá
    items = items.sortedBy { it.item.position }.map { it.toDomain() },
)

fun ItemWithEvidence.toDomain(): ItemResult = ItemResult(
    id = item.id,
    position = item.position,
    text = item.itemText,
    status = item.result,
    note = item.note,
    evidence = evidence.sortedBy { it.capturedAt }.map { it.toDomain() },
)

fun EvidenceEntity.toDomain(): Evidence = Evidence(
    id = clientUuid,
    filePath = filePath,
    capturedAt = capturedAt,
    latitude = latitude,
    longitude = longitude,
)

// ---- Dominio -> entidad ----
// updatedAt y syncStatus no existen en el dominio: los decide quien escribe en la base.

fun Inspection.toEntity(updatedAt: Long): InspectionEntity = InspectionEntity(
    clientUuid = id,
    templateName = templateName,
    templateVersion = templateVersion,
    siteName = siteName,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    syncStatus = syncStatus,
    lastSyncError = lastSyncError,
    updatedAt = updatedAt,
)

fun ItemResult.toEntity(inspectionId: String, updatedAt: Long): ItemResultEntity = ItemResultEntity(
    id = id,
    inspectionId = inspectionId,
    itemText = text,
    position = position,
    result = status,
    note = note,
    updatedAt = updatedAt,
)

fun Evidence.toEntity(itemResultId: Long, syncStatus: SyncStatus = SyncStatus.PENDING): EvidenceEntity =
    EvidenceEntity(
        clientUuid = id,
        itemResultId = itemResultId,
        filePath = filePath,
        capturedAt = capturedAt,
        latitude = latitude,
        longitude = longitude,
        syncStatus = syncStatus,
    )
