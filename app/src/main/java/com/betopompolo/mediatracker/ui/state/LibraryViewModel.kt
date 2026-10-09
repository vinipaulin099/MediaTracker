package com.betopompolo.mediatracker.ui.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<MediaItem> = emptyList(),
    val query: String = "",
    val kind: MediaKind? = null
) {
    val visibleItems: List<MediaItem> get() = items.filter {
        (kind == null || it.kind == kind) &&
            (it.title.contains(query, ignoreCase = true) || it.creator.orEmpty().contains(query, ignoreCase = true))
    }
}

class LibraryViewModel(
    private val repository: MediaRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState(
        query = savedState["query"] ?: "",
        kind = savedState.get<String>("kind")?.let { MediaKind.valueOf(it) }
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
                repository.items.collect { items ->
                    _state.update { it.copy(loading = false, error = null, items = items) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = "Não foi possível carregar sua coleção. Tente novamente.") }
            }
        }
    }

    fun search(query: String) {
        val value = query.take(120)
        savedState["query"] = value
        _state.update { it.copy(query = value) }
    }

    fun filter(kind: MediaKind?) {
        savedState["kind"] = kind?.name
        _state.update { it.copy(kind = kind) }
    }
}
