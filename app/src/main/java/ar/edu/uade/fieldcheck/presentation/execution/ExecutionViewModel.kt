package ar.edu.uade.fieldcheck.presentation.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ar.edu.uade.fieldcheck.di.AppContainer
import ar.edu.uade.fieldcheck.domain.model.Inspection
import ar.edu.uade.fieldcheck.domain.model.ItemStatus
import ar.edu.uade.fieldcheck.domain.repository.InspectionRepository
import ar.edu.uade.fieldcheck.domain.repository.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExecutionViewModel(
    private val inspectionId: String,
    private val startItemId: Long?,
    private val repository: InspectionRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExecutionUiState>(ExecutionUiState.Loading)
    val uiState: StateFlow<ExecutionUiState> = _uiState.asStateFlow()

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Estado de la pantalla que no viene de la base
    private var inspection: Inspection? = null
    private var currentIndex = 0
    private var noteDraft = ""

    private var loadJob: Job? = null
    private var autoAdvanceJob: Job? = null
    private var saveNoteJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ExecutionUiState.Loading
            repository.observeInspection(inspectionId)
                .catch { _uiState.value = ExecutionUiState.Error }
                .collect { loaded ->
                    if (loaded == null) {
                        _uiState.value = ExecutionUiState.NotFound
                        return@collect
                    }
                    val firstLoad = inspection == null
                    inspection = loaded
                    if (firstLoad) {
                        // Retomar: abre en el ítem pedido o en el primero sin completar
                        currentIndex = startIndex(loaded)
                        noteDraft = loaded.items[currentIndex].note.orEmpty()
                    }
                    publish()
                }
        }
    }

    private fun startIndex(loaded: Inspection): Int {
        val requested = loaded.items.indexOfFirst { it.id == startItemId }
        if (requested >= 0) return requested
        val firstPending = loaded.items.indexOfFirst { !it.isCompleted }
        return if (firstPending >= 0) firstPending else 0
    }

    private fun publish() {
        val current = inspection ?: return
        _uiState.value = ExecutionUiState.Content(current, currentIndex, noteDraft)
    }

    fun onStatusSelected(status: ItemStatus) {
        val content = _uiState.value as? ExecutionUiState.Content ?: return
        val item = content.currentItem
        // Tocar el resultado ya elegido lo deja sin completar (corrige un toque accidental)
        val newStatus = if (item.status == status) null else status
        autoAdvanceJob?.cancel()
        viewModelScope.launch {
            repository.updateItemStatus(inspectionId, item.id, newStatus)
        }
        // Cumple y N/A avanzan solos tras una pausa breve; No cumple se queda para agregar evidencia
        if (newStatus == ItemStatus.COMPLIES || newStatus == ItemStatus.NOT_APPLICABLE) {
            val indexAtTap = currentIndex
            autoAdvanceJob = viewModelScope.launch {
                delay(AUTO_ADVANCE_DELAY_MS)
                if (currentIndex == indexAtTap && !content.isLast) goTo(currentIndex + 1)
            }
        }
    }

    fun onNoteChange(text: String) {
        noteDraft = text
        publish()
        // Se guarda cuando el usuario deja de escribir un momento
        val itemId = (_uiState.value as? ExecutionUiState.Content)?.currentItem?.id ?: return
        saveNoteJob?.cancel()
        saveNoteJob = viewModelScope.launch {
            delay(NOTE_SAVE_DELAY_MS)
            repository.updateItemNote(inspectionId, itemId, text)
        }
    }

    fun onPrevious() = goTo(currentIndex - 1)

    fun onNext() = goTo(currentIndex + 1)

    private fun goTo(index: Int) {
        val current = inspection ?: return
        if (index !in current.items.indices) return
        autoAdvanceJob?.cancel()
        saveNoteNow()
        currentIndex = index
        noteDraft = current.items[index].note.orEmpty()
        publish()
    }

    // Si quedó una nota sin guardar al cambiar de ítem, se guarda ya
    private fun saveNoteNow() {
        val current = inspection ?: return
        val item = current.items[currentIndex]
        if (saveNoteJob?.isActive == true || noteDraft != item.note.orEmpty()) {
            saveNoteJob?.cancel()
            val text = noteDraft
            viewModelScope.launch { repository.updateItemNote(inspectionId, item.id, text) }
        }
    }

    companion object {
        private const val AUTO_ADVANCE_DELAY_MS = 400L
        private const val NOTE_SAVE_DELAY_MS = 500L

        fun factory(container: AppContainer, inspectionId: String, startItemId: Long?) = viewModelFactory {
            initializer {
                ExecutionViewModel(inspectionId, startItemId, container.inspectionRepository, container.networkMonitor)
            }
        }
    }
}
