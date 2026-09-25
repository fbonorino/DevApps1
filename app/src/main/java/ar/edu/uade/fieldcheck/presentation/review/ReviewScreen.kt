package ar.edu.uade.fieldcheck.presentation.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.data.repository.FakeInspectionRepository
import ar.edu.uade.fieldcheck.data.repository.FakeScenario
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemResult
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.usecase.FinalizeInspectionUseCase
import ar.edu.uade.fieldcheck.domain.usecase.MissingReason
import ar.edu.uade.fieldcheck.presentation.components.ErrorState
import ar.edu.uade.fieldcheck.presentation.components.ItemStatusLabel
import ar.edu.uade.fieldcheck.presentation.components.LoadingState
import ar.edu.uade.fieldcheck.presentation.components.OfflineBanner
import ar.edu.uade.fieldcheck.presentation.components.PrimaryBottomButton
import ar.edu.uade.fieldcheck.presentation.components.StatusCountRow
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme
import ar.edu.uade.fieldcheck.presentation.theme.statusColors

// P4 · Revisión: toda la inspección de un vistazo, faltantes y finalizar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    isOffline: Boolean,
    onBack: () -> Unit,
    onItemClick: (ItemResult) -> Unit,
    onFinishClick: () -> Unit,
    onConfirmFinish: () -> Unit,
    onDismissDialog: () -> Unit,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.review_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    },
                )
                if (isOffline) OfflineBanner()
            }
        },
        bottomBar = {
            if (uiState is ReviewUiState.Content) {
                PrimaryBottomButton(text = stringResource(R.string.review_finish), onClick = onFinishClick)
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (uiState) {
            ReviewUiState.Loading -> LoadingState(modifier)
            ReviewUiState.NotFound -> ErrorState(
                title = stringResource(R.string.inspection_not_found_title),
                message = stringResource(R.string.inspection_not_found),
                onRetry = onGoHome,
                actionLabel = stringResource(R.string.action_go_home),
                modifier = modifier,
            )
            ReviewUiState.Error -> ErrorState(
                title = stringResource(R.string.inspection_error_title),
                message = stringResource(R.string.inspection_error_message),
                onRetry = onRetry,
                modifier = modifier,
            )
            is ReviewUiState.Content -> ReviewContent(uiState, onItemClick, modifier)
        }
    }

    if (uiState is ReviewUiState.Content && uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.review_confirm_title)) },
            text = { Text(stringResource(R.string.review_confirm_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmFinish) { Text(stringResource(R.string.review_confirm_ok)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun ReviewContent(
    content: ReviewUiState.Content,
    onItemClick: (ItemResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val missing = content.missing
    // Con faltantes, la lista muestra solo esos ítems con su motivo
    val rows: List<Pair<ItemResult, MissingReason?>> =
        missing?.map { it.item to it.reason } ?: content.inspection.items.map { it to null }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            StatusCountRow(content.inspection, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        if (missing != null) {
            item { MissingCard(missing.size) }
        }
        items(rows, key = { it.first.id }) { (item, reason) ->
            ReviewItemRow(item, reason, onClick = { onItemClick(item) })
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun MissingCard(count: Int) {
    val colors = MaterialTheme.statusColors
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.notComplies, contentColor = colors.onNotComplies),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // TalkBack lo anuncia apenas aparece
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
            Column {
                Text(pluralStringResource(R.plurals.review_missing, count, count), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.review_missing_hint), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ReviewItemRow(item: ItemResult, missingReason: MissingReason?, onClick: () -> Unit) {
    val hasProblem = !item.isCompleted || item.needsEvidence
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.review_item_number, item.position), style = MaterialTheme.typography.titleMedium)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ItemStatusLabel(item.status)
                if (item.evidence.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = stringResource(R.string.review_has_photos),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            // "Sin completar" ya lo dice la etiqueta del resultado; el motivo solo suma en "No cumple"
            if (missingReason == MissingReason.NEEDS_EVIDENCE) {
                Text(
                    stringResource(R.string.review_reason_evidence),
                    color = MaterialTheme.statusColors.notComplies,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (hasProblem) {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = stringResource(R.string.review_needs_attention),
                tint = MaterialTheme.statusColors.notComplies,
            )
        }
    }
}

// --- Previews ---

private val finalizeRules = FinalizeInspectionUseCase(FakeInspectionRepository(FakeScenario.CONTENT))

private val inProgress: Inspection = FakeData.inspections().first().let { inspection ->
    val items = inspection.items.toMutableList()
    items[4] = items[4].copy(status = ItemStatus.NOT_COMPLIES)
    inspection.copy(items = items)
}

private val complete: Inspection = inProgress.copy(
    items = inProgress.items.map { it.copy(status = ItemStatus.COMPLIES) },
)

@Composable
private fun ReviewPreview(uiState: ReviewUiState, isOffline: Boolean = false) {
    FieldCheckTheme {
        ReviewScreen(
            uiState = uiState, isOffline = isOffline,
            onBack = {}, onItemClick = {}, onFinishClick = {}, onConfirmFinish = {},
            onDismissDialog = {}, onRetry = {}, onGoHome = {},
        )
    }
}

@Preview(name = "P4 / Carga")
@Composable
private fun LoadingPreview() = ReviewPreview(ReviewUiState.Loading)

@Preview(name = "P4 / Contenido completo")
@Composable
private fun CompletePreview() = ReviewPreview(ReviewUiState.Content(complete))

@Preview(name = "P4 / Con ítems pendientes")
@Composable
private fun PendingPreview() = ReviewPreview(ReviewUiState.Content(inProgress))

@Preview(name = "P4 / Validación con faltantes")
@Composable
private fun MissingPreview() =
    ReviewPreview(ReviewUiState.Content(inProgress, missing = finalizeRules.missingItems(inProgress)))

@Preview(name = "P4 / Confirmar")
@Composable
private fun ConfirmPreview() = ReviewPreview(ReviewUiState.Content(complete, showConfirmDialog = true))

@Preview(name = "P4 / Sin conexión")
@Composable
private fun OfflinePreview() = ReviewPreview(ReviewUiState.Content(complete), isOffline = true)

@Preview(name = "P4 / Error")
@Composable
private fun ErrorPreview() = ReviewPreview(ReviewUiState.Error)
