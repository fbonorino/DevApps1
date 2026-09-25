package ar.edu.uade.fieldcheck.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import ar.edu.uade.fieldcheck.domain.usecase.FinalizeInspectionUseCase
import ar.edu.uade.fieldcheck.domain.usecase.FinalizeResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val inspectionId: String,
    private val repository: InspectionRepository,
    private val finalizeInspection: FinalizeInspectionUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _events = Channel<ReviewEvent>(Channel.BUFFERED)
    val events: Flow<ReviewEvent> = _events.receiveAsFlow()

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            repository.observeInspection(inspectionId)
                .catch { _uiState.value = ReviewUiState.Error }
                .collect { inspection ->
                    if (inspection == null) {
                        _uiState.value = ReviewUiState.NotFound
                        return@collect
                    }
                    val previous = _uiState.value as? ReviewUiState.Content
                    // Si ya se mostraron faltantes, se recalculan con los datos nuevos
                    val missing = previous?.missing?.let {
                        finalizeInspection.missingItems(inspection).ifEmpty { null }
                    }
                    _uiState.value = ReviewUiState.Content(inspection, missing, previous?.showConfirmDialog ?: false)
                }
        }
    }

    fun onFinishClick() {
        val content = _uiState.value as? ReviewUiState.Content ?: return
        val missing = finalizeInspection.missingItems(content.inspection)
        // No finaliza: muestra qué falta. El botón no se deshabilita porque no explicaría por qué.
        _uiState.value = if (missing.isEmpty()) {
            content.copy(missing = null, showConfirmDialog = true)
        } else {
            content.copy(missing = missing)
        }
    }

    fun onDismissDialog() {
        _uiState.update { (it as? ReviewUiState.Content)?.copy(showConfirmDialog = false) ?: it }
    }

    fun onConfirmFinish() {
        val content = _uiState.value as? ReviewUiState.Content ?: return
        onDismissDialog()
        viewModelScope.launch {
            when (val result = finalizeInspection(content.inspection)) {
                FinalizeResult.Finished -> _events.send(ReviewEvent.Finished(wasOffline = isOffline.value))
                is FinalizeResult.Missing -> _uiState.update {
                    (it as? ReviewUiState.Content)?.copy(missing = result.items) ?: it
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, inspectionId: String) = viewModelFactory {
            initializer {
                ReviewViewModel(
                    inspectionId,
                    container.inspectionRepository,
                    container.finalizeInspection,
                    container.networkMonitor,
                )
            }
        }
    }
}
