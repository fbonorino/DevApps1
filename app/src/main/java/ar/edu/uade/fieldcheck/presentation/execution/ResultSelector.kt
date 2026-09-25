package ar.edu.uade.fieldcheck.presentation.execution

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.presentation.components.itemStatusStyle
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

// Tres botones grandes (64 dp) que se comportan como un grupo de opciones para TalkBack
@Composable
fun ResultSelector(
    selected: ItemStatus?,
    onSelect: (ItemStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ItemStatus.entries.forEach { status ->
            ResultOption(
                status = status,
                isSelected = selected == status,
                onClick = { onSelect(status) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ResultOption(
    status: ItemStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = itemStatusStyle(status)
    val shape = MaterialTheme.shapes.medium
    // Seleccionado: fondo lleno. No seleccionado: solo contorno.
    val background = if (isSelected) style.color else MaterialTheme.colorScheme.surface
    val content = if (isSelected) style.onColor else style.color

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(shape)
            .background(background)
            .border(BorderStroke(2.dp, style.color), shape)
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Icon(style.icon, contentDescription = null, tint = content)
            Text(style.label, color = content, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultSelectorPreview() {
    FieldCheckTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultSelector(selected = null, onSelect = {})
            ResultSelector(selected = ItemStatus.COMPLIES, onSelect = {})
            ResultSelector(selected = ItemStatus.NOT_COMPLIES, onSelect = {})
        }
    }
}
