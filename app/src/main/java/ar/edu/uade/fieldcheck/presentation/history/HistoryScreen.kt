package ar.edu.uade.fieldcheck.presentation.history

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.presentation.components.EmptyState
import ar.edu.uade.fieldcheck.presentation.components.ErrorState
import ar.edu.uade.fieldcheck.presentation.components.OfflineBanner
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme

// P1 · Historial. Stateless: recibe el estado y avisa los eventos con callbacks.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    isOffline: Boolean,
    onNewInspection: () -> Unit,
    onInspectionClick: (Inspection) -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
                if (isOffline) OfflineBanner()
            }
        },
        // Acción principal abajo a la derecha, al alcance del pulgar. Sigue visible en vacío y error.
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewInspection,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.history_new_inspection)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (uiState) {
            HistoryUiState.Loading -> SkeletonList(modifier)
            HistoryUiState.Empty -> EmptyState(
                icon = Icons.Outlined.AssignmentTurnedIn,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
                modifier = modifier,
            )
            HistoryUiState.Error -> ErrorState(
                title = stringResource(R.string.history_error_title),
                message = stringResource(R.string.history_error_message),
                onRetry = onRetry,
                modifier = modifier,
            )
            is HistoryUiState.Content -> InspectionList(uiState, onInspectionClick, modifier)
        }
    }
}

@Composable
private fun InspectionList(
    content: HistoryUiState.Content,
    onInspectionClick: (Inspection) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Espacio abajo para que el FAB no tape la última tarjeta
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // "En curso" va primero: es lo que el inspector tiene que retomar
        if (content.inProgress.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.history_section_in_progress)) }
            items(content.inProgress, key = { it.id }) { inspection ->
                InspectionCard(inspection, onClick = { onInspectionClick(inspection) })
            }
        }
        if (content.finished.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.history_section_finished)) }
            items(content.finished, key = { it.id }) { inspection ->
                InspectionCard(inspection, onClick = { onInspectionClick(inspection) })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { heading() },
    )
}

// Tres tarjetas grises mientras se lee la base local
@Composable
private fun SkeletonList(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.history_loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

// --- Previews: un estado por preview ---

private fun previewContent(): HistoryUiState.Content {
    val inspections = FakeData.inspections()
    return HistoryUiState.Content(
        inProgress = inspections.filter { it.status == InspectionStatus.IN_PROGRESS },
        finished = inspections.filter { it.status == InspectionStatus.FINISHED },
    )
}

@Composable
private fun HistoryPreview(uiState: HistoryUiState, isOffline: Boolean = false) {
    FieldCheckTheme {
        HistoryScreen(uiState, isOffline, onNewInspection = {}, onInspectionClick = {}, onRetry = {})
    }
}

@Preview(name = "P1 / Carga")
@Composable
private fun HistoryLoadingPreview() = HistoryPreview(HistoryUiState.Loading)

@Preview(name = "P1 / Contenido")
@Composable
private fun HistoryContentPreview() = HistoryPreview(previewContent())

@Preview(name = "P1 / Vacío")
@Composable
private fun HistoryEmptyPreview() = HistoryPreview(HistoryUiState.Empty)

@Preview(name = "P1 / Error")
@Composable
private fun HistoryErrorPreview() = HistoryPreview(HistoryUiState.Error)

@Preview(name = "P1 / Sin conexión")
@Composable
private fun HistoryOfflinePreview() = HistoryPreview(previewContent(), isOffline = true)

@Preview(name = "P1 / Contenido oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryDarkPreview() = HistoryPreview(previewContent())
