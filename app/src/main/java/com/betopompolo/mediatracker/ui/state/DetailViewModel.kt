package com.betopompolo.mediatracker.ui.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.model.MediaItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val item: MediaItem? = null,
    val error: String? = null,
    val progress: String = "",
    val rating: String = "",
    val notes: String = "",
    val progressError: String? = null,
    val ratingError: String? = null,
    val notesError: String? = null,
    val saving: Boolean = false,
    val message: String? = null,
    val deleted: Boolean = false
)

class DetailViewModel(
    private val id: String,
    private val repository: MediaRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(DetailUiState(
        progress = savedState["progress"] ?: "", rating = savedState["rating"] ?: "",
        notes = savedState["notes"] ?: ""
    ))
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init { retry() }

    fun retry() {
        loadJob?.cancel()
        _state.update { it.copy(loading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                repository.initialize()
                repository.items.collect { list ->
                    val item = list.firstOrNull { it.id == id }
                    if (item != null && savedState.get<Boolean>("initialized") != true) {
                        saveDraft(item.currentUnits.toString(), item.rating?.toString().orEmpty(), item.notes)
                        savedState["initialized"] = true
                    }
                    _state.update { it.copy(loading = false, item = item, error = null) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = "Não foi possível carregar este item.") }
            }
        }
    }

    fun changeProgress(value: String) {
        if (!_state.value.saving) saveDraft(value.take(10), _state.value.rating, _state.value.notes)
    }
    fun changeRating(value: String) {
        if (!_state.value.saving) saveDraft(_state.value.progress, value.take(3), _state.value.notes)
    }
    fun changeNotes(value: String) {
        if (!_state.value.saving) saveDraft(_state.value.progress, _state.value.rating, value.take(1001))
    }

    private fun saveDraft(progress: String, rating: String, notes: String) {
        savedState["progress"] = progress
        savedState["rating"] = rating
        savedState["notes"] = notes
        _state.update { it.copy(progress = progress, rating = rating, notes = notes,
            progressError = null, ratingError = null, notesError = null, message = null) }
    }

    fun save() {
        val current = _state.value
        val item = current.item ?: return
        if (current.saving) return
        val progress = current.progress.toIntOrNull()
        val rating = current.rating.toIntOrNull()
        val progressError = if (progress !in 0..item.totalUnits) "Use um valor entre 0 e ${item.totalUnits}." else null
        val ratingError = if (current.rating.isNotBlank() && rating !in 1..5) "Use uma nota de 1 a 5 ou deixe vazio." else null
        val notesError = if (current.notes.length > 1000) "Use até 1.000 caracteres." else null
        _state.update { it.copy(progressError = progressError, ratingError = ratingError, notesError = notesError) }
        if (listOf(progressError, ratingError, notesError).any { it != null }) return
        mutate("Acompanhamento salvo.") { repository.updateTracking(id, requireNotNull(progress), rating, current.notes) }
    }

    fun remove() {
        if (_state.value.saving) return
        mutate(null) {
            repository.remove(id)
            _state.update { it.copy(deleted = true) }
        }
    }

    private fun mutate(success: String?, action: suspend () -> Unit) {
        _state.update { it.copy(saving = true, message = null) }
        viewModelScope.launch {
            try {
                action()
                _state.update { it.copy(saving = false, message = success) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(saving = false, message = "Não foi possível gravar a alteração. Tente novamente.") }
            }
        }
    }
    fun consumeMessage() { _state.update { it.copy(message = null) } }
    fun acknowledgeDeleted() { _state.update { it.copy(deleted = false) } }
}
