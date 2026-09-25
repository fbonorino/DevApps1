package ar.edu.uade.fieldcheck.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme
import ar.edu.uade.fieldcheck.presentation.theme.statusColors

// Franja fija bajo la barra superior. No se cierra: desaparece sola cuando vuelve la red.
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.statusColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.offlineBanner)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            // TalkBack lo anuncia cuando aparece
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ícono decorativo: el texto de al lado ya dice lo mismo
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = colors.onOfflineBanner)
        Text(
            text = stringResource(R.string.offline_banner),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onOfflineBanner,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    FieldCheckTheme { OfflineBanner() }
}
