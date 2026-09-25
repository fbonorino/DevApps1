package ar.edu.uade.fieldcheck.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.presentation.theme.statusColors

// Ícono, texto y colores de cada resultado. Se define una sola vez y lo usan P3, P4 y P5.
data class ItemStatusStyle(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onColor: Color,
)

@Composable
fun itemStatusStyle(status: ItemStatus?): ItemStatusStyle {
    val colors = MaterialTheme.statusColors
    return when (status) {
        ItemStatus.COMPLIES -> ItemStatusStyle(
            Icons.Filled.Check, stringResource(R.string.status_complies), colors.complies, colors.onComplies,
        )
        ItemStatus.NOT_COMPLIES -> ItemStatusStyle(
            Icons.Filled.Close, stringResource(R.string.status_not_complies), colors.notComplies, colors.onNotComplies,
        )
        ItemStatus.NOT_APPLICABLE -> ItemStatusStyle(
            Icons.Filled.Remove, stringResource(R.string.status_not_applicable), colors.notApplicable, colors.onNotApplicable,
        )
        null -> ItemStatusStyle(
            Icons.Outlined.RadioButtonUnchecked,
            stringResource(R.string.status_pending),
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surface,
        )
    }
}
