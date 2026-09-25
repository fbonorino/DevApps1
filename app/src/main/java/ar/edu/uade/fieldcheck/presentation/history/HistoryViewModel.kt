package ar.edu.uade.fieldcheck.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.domain.model.InspectionStatus
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val inspectionRepository: InspectionRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        // Si ya había una lectura en curso la cancelamos, para no tener dos colectando a la vez
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            inspectionRepository.observeInspections()
                .catch { _uiState.value = HistoryUiState.Error }
                .collect { inspections ->
                    _uiState.value = if (inspections.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Content(
                            inProgress = inspections.filter { it.status == InspectionStatus.IN_PROGRESS },
                            finished = inspections.filter { it.status == InspectionStatus.FINISHED },
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HistoryViewModel(container.inspectionRepository, container.networkMonitor) }
        }
    }
}
