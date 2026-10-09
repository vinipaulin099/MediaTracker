package com.betopompolo.mediatracker.ui.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.model.FormErrors
import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaKind
import com.betopompolo.mediatracker.model.MediaValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormUiState(
    val draft: MediaDraft = MediaDraft(),
    val errors: FormErrors = FormErrors(),
    val saving: Boolean = false,
    val error: String? = null,
    val savedId: String? = null
)

class FormViewModel(
    private val repository: MediaRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(FormUiState(draft = MediaDraft(
        title = savedState["title"] ?: "", totalUnits = savedState["total"] ?: "",
        creator = savedState["creator"] ?: "", year = savedState["year"] ?: "",
        kind = MediaKind.valueOf(savedState["kind"] ?: MediaKind.BOOK.name)
    )))
    val state = _state.asStateFlow()

    fun change(draft: MediaDraft) {
        if (_state.value.saving) return
        val bounded = draft.copy(title = draft.title.take(121), creator = draft.creator.take(101),
            totalUnits = draft.totalUnits.take(10), year = draft.year.take(10))
        savedState["title"] = bounded.title
        savedState["total"] = bounded.totalUnits
        savedState["creator"] = bounded.creator
        savedState["year"] = bounded.year
        savedState["kind"] = bounded.kind.name
        _state.update { it.copy(draft = bounded, errors = FormErrors(), error = null) }
    }

    fun submit() {
        if (_state.value.saving || _state.value.savedId != null) return
        val draft = _state.value.draft
        val errors = MediaValidator.validate(draft)
        _state.update { it.copy(errors = errors, error = null) }
        if (errors.hasErrors) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                val id = repository.add(draft)
                _state.update { it.copy(saving = false, savedId = id) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(saving = false, error = "Não foi possível salvar. Seus campos foram mantidos; tente novamente.") }
            }
        }
    }

    fun acknowledgeSaved() { _state.update { it.copy(savedId = null) } }
}
