package ar.edu.uade.fieldcheck.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

// Resultado de un ítem en modo lectura: ícono + texto en el color del estado
@Composable
fun ItemStatusLabel(status: ItemStatus?, modifier: Modifier = Modifier) {
    val style = itemStatusStyle(status)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(18.dp))
        Text(style.label, color = style.color, style = MaterialTheme.typography.labelLarge)
    }
}

// Resumen de conteos ("Cumple 15", "No cumple 3", ...). Lo usan P4 y P5.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusCountRow(inspection: Inspection, modifier: Modifier = Modifier) {
    val pending = inspection.totalCount - inspection.completedCount
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ItemStatus.entries.forEach { status -> CountChip(status, inspection.count(status)) }
        if (pending > 0) CountChip(null, pending)
    }
}

@Composable
private fun CountChip(status: ItemStatus?, count: Int) {
    val style = itemStatusStyle(status)
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, style.color),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(18.dp))
            Text(
                stringResource(R.string.status_count, style.label, count),
                color = style.color,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusCountRowPreview() {
    FieldCheckTheme {
        StatusCountRow(FakeData.inspections().first(), Modifier.padding(16.dp))
    }
}
