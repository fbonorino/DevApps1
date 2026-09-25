package ar.edu.uade.fieldcheck.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.domain.model.SyncStatus
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme
import ar.edu.uade.fieldcheck.presentation.theme.statusColors

// Chip de estado de sincronización: siempre ícono + texto, nunca solo color
@Composable
fun SyncChip(status: SyncStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.statusColors
    val (icon: ImageVector, textRes: Int, color: Color) = when (status) {
        SyncStatus.PENDING -> Triple(Icons.Outlined.Schedule, R.string.sync_pending, MaterialTheme.colorScheme.onSurfaceVariant)
        SyncStatus.SYNCED -> Triple(Icons.Filled.CloudDone, R.string.sync_synced, colors.complies)
        SyncStatus.FAILED -> Triple(Icons.Outlined.ErrorOutline, R.string.sync_failed, colors.notComplies)
    }
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncChipPreview() {
    FieldCheckTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SyncStatus.entries.forEach { SyncChip(it) }
        }
    }
}
