package ar.edu.uade.fieldcheck.presentation.newinspection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.uade.fieldcheck.R
import ar.edu.uade.fieldcheck.data.repository.FakeData
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.presentation.components.EmptyState
import ar.edu.uade.fieldcheck.presentation.components.ErrorState
import ar.edu.uade.fieldcheck.presentation.components.LoadingState
import ar.edu.uade.fieldcheck.presentation.components.OfflineBanner
import ar.edu.uade.fieldcheck.presentation.components.formatTime
import ar.edu.uade.fieldcheck.presentation.theme.FieldCheckTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// P2 · Nueva inspección: elegir plantilla + ingresar sitio
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    uiState: NewInspectionUiState,
    isOffline: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onStartInspection: (templateId: String, siteName: String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // Plantilla elegida para la hoja inferior (null = hoja cerrada)
    var selectedTemplateId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.new_inspection_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    },
                )
                if (isOffline) OfflineBanner()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (uiState) {
            NewInspectionUiState.Loading -> LoadingState(modifier, stringResource(R.string.templates_downloading))
            is NewInspectionUiState.Empty -> if (uiState.needsConnection) {
                EmptyState(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.templates_empty_offline_title),
                    message = stringResource(R.string.templates_empty_offline_message),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRefresh,
                    modifier = modifier,
                )
            } else {
                EmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = stringResource(R.string.templates_empty_title),
                    message = stringResource(R.string.templates_empty_message),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRefresh,
                    modifier = modifier,
                )
            }
            NewInspectionUiState.Error -> ErrorState(
                title = stringResource(R.string.templates_error_title),
                message = stringResource(R.string.templates_error_message),
                onRetry = onRefresh,
                modifier = modifier,
            )
            is NewInspectionUiState.Content -> PullToRefreshBox(
                // El indicador de "actualizando" es la barra fina de arriba, no el del gesto
                isRefreshing = false,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize(),
            ) {
                TemplateList(uiState, isOffline, onTemplateClick = { selectedTemplateId = it.id })
            }
        }
    }

    val selected = (uiState as? NewInspectionUiState.Content)?.templates?.find { it.id == selectedTemplateId }
    if (selected != null) {
        StartInspectionSheet(
            templateName = selected.name,
            onStart = { site ->
                selectedTemplateId = null
                onStartInspection(selected.id, site)
            },
            onDismiss = { selectedTemplateId = null },
        )
    }
}

@Composable
private fun TemplateList(
    content: NewInspectionUiState.Content,
    isOffline: Boolean,
    onTemplateClick: (Template) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        if (content.isRefreshing) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                val age = templatesAgeText(content.lastUpdatedAt)
                Text(
                    text = if (isOffline) "$age. ${stringResource(R.string.templates_stale_offline)}" else age,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(content.templates, key = { it.id }) { template ->
                TemplateCard(template, onClick = { onTemplateClick(template) })
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Template, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(template.name, style = MaterialTheme.typography.titleMedium)
            val items = pluralStringResource(R.plurals.template_item_count, template.items.size, template.items.size)
            Text(
                text = "$items · ${stringResource(R.string.template_version, template.version)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// "Plantillas actualizadas hoy, 09:14" o "hace 3 días"
@Composable
private fun templatesAgeText(downloadedAt: Long): String {
    val zone = ZoneId.systemDefault()
    val day = Instant.ofEpochMilli(downloadedAt).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(day, LocalDate.now(zone)).toInt()
    return if (days <= 0) {
        stringResource(R.string.templates_updated_today, formatTime(downloadedAt))
    } else {
        pluralStringResource(R.plurals.templates_updated_days_ago, days, days)
    }
}

// --- Previews ---

private fun previewContent(daysAgo: Int = 0, isRefreshing: Boolean = false): NewInspectionUiState.Content {
    val downloadedAt = System.currentTimeMillis() - daysAgo * 24 * 60 * 60 * 1000L
    return NewInspectionUiState.Content(FakeData.templates(downloadedAt), downloadedAt, isRefreshing)
}

@Composable
private fun NewInspectionPreview(uiState: NewInspectionUiState, isOffline: Boolean = false) {
    FieldCheckTheme {
        NewInspectionScreen(uiState, isOffline, onBack = {}, onRefresh = {}, onStartInspection = { _, _ -> })
    }
}

@Preview(name = "P2 / Carga")
@Composable
private fun LoadingPreview() = NewInspectionPreview(NewInspectionUiState.Loading)

@Preview(name = "P2 / Contenido")
@Composable
private fun ContentPreview() = NewInspectionPreview(previewContent())

@Preview(name = "P2 / Actualizando")
@Composable
private fun RefreshingPreview() = NewInspectionPreview(previewContent(isRefreshing = true))

@Preview(name = "P2 / Desactualizado sin conexión")
@Composable
private fun StaleOfflinePreview() = NewInspectionPreview(previewContent(daysAgo = 3), isOffline = true)

@Preview(name = "P2 / Vacío sin conexión")
@Composable
private fun EmptyOfflinePreview() = NewInspectionPreview(NewInspectionUiState.Empty(needsConnection = true), isOffline = true)

@Preview(name = "P2 / Vacío")
@Composable
private fun EmptyPreview() = NewInspectionPreview(NewInspectionUiState.Empty(needsConnection = false))

@Preview(name = "P2 / Error")
@Composable
private fun ErrorPreview() = NewInspectionPreview(NewInspectionUiState.Error)
