package ar.edu.uade.fieldcheck.presentation.newinspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import ar.edu.uade.fieldcheck.domain.repository.TemplateRepository
import ar.edu.uade.fieldcheck.domain.usecase.CreateInspectionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewInspectionViewModel(
    private val templateRepository: TemplateRepository,
    private val networkMonitor: NetworkMonitor,
    private val createInspection: CreateInspectionUseCase,
) : ViewModel() {

    private enum class RefreshState { IDLE, REFRESHING, FAILED }

    private val refreshState = MutableStateFlow(RefreshState.REFRESHING)

    private val _events = Channel<NewInspectionEvent>(Channel.BUFFERED)
    val events: Flow<NewInspectionEvent> = _events.receiveAsFlow()

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // La lista sale siempre de la caché; la red solo la actualiza
    val uiState: StateFlow<NewInspectionUiState> = combine(
        templateRepository.observeTemplates(),
        refreshState,
        networkMonitor.isOnline,
    ) { templates, refresh, online ->
        when {
            templates.isNotEmpty() -> NewInspectionUiState.Content(
                templates = templates,
                lastUpdatedAt = templates.minOf { it.downloadedAt },
                isRefreshing = refresh == RefreshState.REFRESHING,
            )
            refresh == RefreshState.REFRESHING -> NewInspectionUiState.Loading
            !online -> NewInspectionUiState.Empty(needsConnection = true)
            refresh == RefreshState.FAILED -> NewInspectionUiState.Error
            else -> NewInspectionUiState.Empty(needsConnection = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewInspectionUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Sin conexión no intentamos: se usa lo guardado
            if (!networkMonitor.isOnline.first()) {
                refreshState.value = RefreshState.FAILED
                return@launch
            }
            refreshState.value = RefreshState.REFRESHING
            try {
                templateRepository.refreshTemplates()
                refreshState.value = RefreshState.IDLE
            } catch (e: Exception) {
                refreshState.value = RefreshState.FAILED
                // Si había lista guardada no bloqueamos: solo avisamos
                if (uiState.value is NewInspectionUiState.Content) {
                    _events.send(NewInspectionEvent.RefreshFailed)
                }
            }
        }
    }

    fun startInspection(templateId: String, siteName: String) {
        val content = uiState.value as? NewInspectionUiState.Content ?: return
        val template = content.templates.find { it.id == templateId } ?: return
        viewModelScope.launch {
            val inspectionId = createInspection(template, siteName)
            _events.send(NewInspectionEvent.InspectionCreated(inspectionId))
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                NewInspectionViewModel(
                    container.templateRepository,
                    container.networkMonitor,
                    container.createInspection,
                )
            }
        }
    }
}
