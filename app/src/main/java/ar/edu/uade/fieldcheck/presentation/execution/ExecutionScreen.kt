package ar.edu.uade.fieldcheck.presentation.execution

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.presentation.components.ErrorState
import ar.edu.uade.fieldcheck.presentation.components.LoadingState
import ar.edu.uade.fieldcheck.presentation.components.OfflineBanner
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme
import ar.edu.uade.fieldcheck.presentation.theme.statusColors

// P3 · Ejecución: un ítem por vez. Arriba se lee, abajo (zona del pulgar) se responde.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionScreen(
    uiState: ExecutionUiState,
    isOffline: Boolean,
    onBack: () -> Unit,
    onSeeAll: () -> Unit,
    onStatusSelected: (ItemStatus) -> Unit,
    onNoteChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val content = uiState as? ExecutionUiState.Content
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(content?.inspection?.siteName.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    },
                    actions = {
                        if (content != null) {
                            IconButton(onClick = onSeeAll) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.execution_see_all))
                            }
                        }
                    },
                )
                if (content != null) ProgressHeader(content)
                if (isOffline) OfflineBanner()
            }
        },
        bottomBar = {
            if (content != null) {
                ThumbZone(content, onStatusSelected, onPrevious, onNext, onSeeAll)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (uiState) {
            ExecutionUiState.Loading -> LoadingState(modifier, stringResource(R.string.execution_loading))
            ExecutionUiState.NotFound -> ErrorState(
                title = stringResource(R.string.inspection_not_found_title),
                message = stringResource(R.string.inspection_not_found),
                onRetry = onBack,
                actionLabel = stringResource(R.string.action_go_home),
                modifier = modifier,
            )
            ExecutionUiState.Error -> ErrorState(
                title = stringResource(R.string.inspection_error_title),
                message = stringResource(R.string.inspection_error_message),
                onRetry = onRetry,
                modifier = modifier,
            )
            is ExecutionUiState.Content -> ItemContent(
                content = uiState,
                onNoteChange = onNoteChange,
                onAddPhoto = onAddPhoto,
                modifier = modifier
                    .consumeWindowInsets(padding)
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun ProgressHeader(content: ExecutionUiState.Content) {
    val inspection = content.inspection
    val progressDescription = stringResource(
        R.string.execution_progress_description, inspection.completedCount, inspection.totalCount,
    )
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.execution_item_position, content.currentIndex + 1, inspection.totalCount),
            style = MaterialTheme.typography.labelLarge,
        )
        // La barra refleja los ítems completados (RF02), no la posición
        LinearProgressIndicator(
            progress = { inspection.completedCount.toFloat() / inspection.totalCount.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .semantics { contentDescription = progressDescription },
        )
    }
}

@Composable
private fun ItemContent(
    content: ExecutionUiState.Content,
    onNoteChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = content.currentItem
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Zona de lectura. TalkBack anuncia el ítem nuevo cuando avanza solo.
        Text(
            text = item.text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (item.isCompleted) SavedLabel()

        // Zona de evidencia
        OutlinedTextField(
            value = content.noteDraft,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.execution_note_label)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        EvidenceRow(onAddPhoto)
        if (content.showEvidenceWarning) EvidenceWarning()
    }
}

@Composable
private fun SavedLabel() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.statusColors.complies, modifier = Modifier.size(20.dp))
        Text(
            stringResource(R.string.execution_saved),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Fila de fotos. Por ahora solo el botón: la cámara real viene en la etapa de implementación.
@Composable
private fun EvidenceRow(onAddPhoto: () -> Unit) {
    OutlinedButton(onClick = onAddPhoto, modifier = Modifier.heightIn(min = 48.dp)) {
        Icon(Icons.Outlined.CameraAlt, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.execution_add_photo))
    }
}

@Composable
private fun EvidenceWarning() {
    val color = MaterialTheme.statusColors.notComplies
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = color)
        Text(stringResource(R.string.execution_needs_evidence), color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

// Zona del pulgar, fija abajo: resultado + navegación entre ítems
@Composable
private fun ThumbZone(
    content: ExecutionUiState.Content,
    onStatusSelected: (ItemStatus) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReview: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ResultSelector(selected = content.currentItem.status, onSelect = onStatusSelected)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onPrevious,
                    enabled = !content.isFirst,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.execution_previous))
                }
                if (content.isLast) {
                    Button(onClick = onReview, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.execution_review))
                    }
                } else {
                    Button(onClick = onNext, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.execution_next))
                    }
                }
            }
        }
    }
}

// --- Previews ---

private val previewInspection = FakeData.inspections().first()

@Composable
private fun ExecutionPreview(uiState: ExecutionUiState, isOffline: Boolean = false) {
    FieldCheckTheme {
        ExecutionScreen(
            uiState = uiState,
            isOffline = isOffline,
            onBack = {}, onSeeAll = {}, onStatusSelected = {}, onNoteChange = {},
            onAddPhoto = {}, onPrevious = {}, onNext = {}, onRetry = {},
        )
    }
}

@Preview(name = "P3 / Carga")
@Composable
private fun LoadingPreview() = ExecutionPreview(ExecutionUiState.Loading)

@Preview(name = "P3 / Ítem sin completar")
@Composable
private fun PendingPreview() = ExecutionPreview(ExecutionUiState.Content(previewInspection, 4, ""))

@Preview(name = "P3 / Ítem completado")
@Composable
private fun CompletedPreview() = ExecutionPreview(ExecutionUiState.Content(previewInspection, 0, ""))

@Preview(name = "P3 / No cumple sin evidencia")
@Composable
private fun NeedsEvidencePreview() {
    val items = previewInspection.items.toMutableList()
    items[4] = items[4].copy(status = ItemStatus.NOT_COMPLIES)
    ExecutionPreview(ExecutionUiState.Content(previewInspection.copy(items = items), 4, ""))
}

@Preview(name = "P3 / Último ítem")
@Composable
private fun LastItemPreview() = ExecutionPreview(ExecutionUiState.Content(previewInspection, 9, ""))

@Preview(name = "P3 / Sin conexión")
@Composable
private fun OfflinePreview() = ExecutionPreview(ExecutionUiState.Content(previewInspection, 4, ""), isOffline = true)

@Preview(name = "P3 / Error")
@Composable
private fun ErrorPreview() = ExecutionPreview(ExecutionUiState.Error)

@Preview(name = "P3 / No existe")
@Composable
private fun NotFoundPreview() = ExecutionPreview(ExecutionUiState.NotFound)

@Preview(name = "P3 / Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() = ExecutionPreview(ExecutionUiState.Content(previewInspection, 0, ""))
