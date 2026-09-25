package ar.edu.uade.fieldcheck.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.presentation.components.SyncChip
import ar.edu.uade.fieldcheck.presentation.components.formatDateTime
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

// Tarjeta del historial. Toda la tarjeta es tocable.
@Composable
fun InspectionCard(inspection: Inspection, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = inspection.siteName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = inspection.templateName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDateTime(inspection.finishedAt ?: inspection.startedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (inspection.status == InspectionStatus.IN_PROGRESS) {
                Text(
                    text = stringResource(R.string.history_progress, inspection.completedCount, inspection.totalCount),
                    style = MaterialTheme.typography.labelLarge,
                )
                LinearProgressIndicator(
                    progress = { inspection.completedCount.toFloat() / inspection.totalCount.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Una inspección en curso no muestra chip: todavía no se sincroniza
                SyncChip(inspection.syncStatus)
            }
        }
    }
}

@Preview
@Composable
private fun InspectionCardPreview() {
    FieldCheckTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FakeData.inspections().take(2).forEach { InspectionCard(it, onClick = {}) }
        }
    }
}
