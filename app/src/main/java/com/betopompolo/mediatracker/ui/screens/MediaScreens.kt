package com.betopompolo.mediatracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.betopompolo.mediatracker.R
import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind
import com.betopompolo.mediatracker.ui.state.DetailUiState
import com.betopompolo.mediatracker.ui.state.FormUiState
import com.betopompolo.mediatracker.ui.state.LibraryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onSearch: (String) -> Unit,
    onFilter: (MediaKind?) -> Unit,
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onCatalog: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Minha coleção") }, actions = {
            TextButton(onClick = onCatalog, modifier = Modifier.heightIn(min = 48.dp)) { Text("Catálogo") }
        }) },
        floatingActionButton = {
            if (!state.loading && state.error == null) ExtendedFloatingActionButton(onClick = onAdd) { Text("Adicionar") }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 840.dp).fillMaxSize().padding(horizontal = 16.dp)) {
                Text("Livros e filmes, no seu ritmo.", style = MaterialTheme.typography.titleMedium)
                Text("${state.items.size} itens • ${state.items.count { it.currentUnits == it.totalUnits }} concluídos",
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(value = state.query, onValueChange = onSearch,
                    label = { Text("Buscar por título ou autor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.kind == null, onClick = { onFilter(null) }, label = { Text("Todos") })
                    MediaKind.entries.forEach { kind ->
                        FilterChip(selected = state.kind == kind, onClick = { onFilter(kind) }, label = { Text(kind.label) })
                    }
                }
                when {
                    state.loading -> LoadingPanel()
                    state.error != null -> MessagePanel("Falha ao carregar", state.error, "Tentar novamente", onRetry)
                    state.visibleItems.isEmpty() -> MessagePanel(
                        if (state.items.isEmpty()) "Sua coleção está vazia" else "Nenhum resultado",
                        if (state.items.isEmpty()) "Adicione um livro ou filme para começar." else "Altere a busca ou o filtro para encontrar outros itens.",
                        if (state.items.isEmpty()) "Adicionar primeiro item" else "Limpar filtros",
                        if (state.items.isEmpty()) onAdd else ({ onSearch(""); onFilter(null) })
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 96.dp)
                    ) {
                        items(state.visibleItems, key = { it.id }) { item ->
                            MediaCard(item) { onOpen(item.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaCard(item: MediaItem, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            MediaCover(item, Modifier.size(width = 64.dp, height = 96.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("${item.kind.label} • ${item.creator ?: "Autor/direção não informados"}", style = MaterialTheme.typography.bodySmall)
                Text("${item.currentUnits} de ${item.totalUnits} ${item.kind.unit} • ${item.progressPercent}%",
                    style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(progress = { item.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                Text(item.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(state: FormUiState, onChange: (MediaDraft) -> Unit, onSubmit: () -> Unit,
    onBack: () -> Unit, onSaved: (String) -> Unit) {
    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }
    Scaffold(topBar = { PageBar("Adicionar à coleção", onBack, enabled = !state.saving) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("O que você quer acompanhar?", style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaKind.entries.forEach { kind ->
                        FilterChip(selected = state.draft.kind == kind, onClick = { onChange(state.draft.copy(kind = kind)) },
                            enabled = !state.saving, label = { Text(kind.label) })
                    }
                }
                InputField("Título *", state.draft.title, { onChange(state.draft.copy(title = it)) },
                    state.errors.title, enabled = !state.saving)
                InputField(if (state.draft.kind == MediaKind.BOOK) "Autor (opcional)" else "Direção (opcional)",
                    state.draft.creator, { onChange(state.draft.copy(creator = it)) }, state.errors.creator, enabled = !state.saving)
                InputField("Total de ${state.draft.kind.unit} *", state.draft.totalUnits,
                    { onChange(state.draft.copy(totalUnits = it)) }, state.errors.totalUnits, numeric = true, enabled = !state.saving)
                InputField("Ano (opcional)", state.draft.year, { onChange(state.draft.copy(year = it)) },
                    state.errors.year, numeric = true, enabled = !state.saving)
                Text("* Campos obrigatórios. O progresso começa em zero e pode ser atualizado nos detalhes.", style = MaterialTheme.typography.bodySmall)
                state.error?.let { ErrorText(it) }
                Button(onClick = onSubmit, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (state.saving) "Salvando…" else "Salvar na coleção")
                }
                if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onProgress: (String) -> Unit,
    onRating: (String) -> Unit,
    onNotes: (String) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onMessageConsumed: () -> Unit,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    var confirmRemoval by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); onMessageConsumed() }
    }
    if (confirmRemoval) AlertDialog(
        onDismissRequest = { confirmRemoval = false }, title = { Text("Remover da coleção?") },
        text = { Text("O progresso, a nota e as anotações deste item serão removidos.") },
        confirmButton = { TextButton(onClick = { confirmRemoval = false; onRemove() }) { Text("Remover") } },
        dismissButton = { TextButton(onClick = { confirmRemoval = false }) { Text("Manter") } }
    )
    Scaffold(topBar = { PageBar("Acompanhamento", onBack, enabled = !state.saving) },
        snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            when {
                state.loading -> LoadingPanel()
                state.error != null -> MessagePanel("Falha ao carregar", state.error, "Tentar novamente", onRetry)
                state.item == null -> MessagePanel("Item não encontrado", "Ele pode ter sido removido da coleção.", "Voltar à coleção", onBack)
                else -> {
                    val item = state.item
                    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MediaCover(item, Modifier.fillMaxWidth().height(200.dp))
                        Text(item.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                        Text("${item.kind.label} • ${item.year?.toString() ?: "Ano não informado"}")
                        SectionCard("Sobre a obra") {
                            Text(item.creator ?: "Autor/direção não informados")
                            Text(item.synopsis ?: "Sinopse não informada.")
                        }
                        SectionCard("Seu progresso") {
                            Text("${item.currentUnits} de ${item.totalUnits} ${item.kind.unit} salvos • ${item.progressPercent}%")
                            LinearProgressIndicator(progress = { item.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                            Text(item.status, color = MaterialTheme.colorScheme.primary)
                            InputField(if (item.kind == MediaKind.BOOK) "Páginas lidas" else "Minutos assistidos",
                                state.progress, onProgress, state.progressError, numeric = true, enabled = !state.saving)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onProgress(((state.progress.toIntOrNull() ?: 0).coerceIn(0, item.totalUnits) - 1).coerceAtLeast(0).toString()) }, enabled = !state.saving) { Text("Diminuir 1") }
                                OutlinedButton(onClick = { onProgress(((state.progress.toIntOrNull() ?: 0).coerceIn(0, item.totalUnits) + 1).coerceAtMost(item.totalUnits).toString()) }, enabled = !state.saving) { Text("Avançar 1") }
                                OutlinedButton(onClick = { onProgress(item.totalUnits.toString()) }, enabled = !state.saving) { Text("Marcar concluído") }
                            }
                        }
                        SectionCard("Sua avaliação") {
                            InputField("Nota de 1 a 5 (opcional)", state.rating, onRating,
                                state.ratingError, numeric = true, enabled = !state.saving)
                            OutlinedTextField(value = state.notes, onValueChange = onNotes, label = { Text("Anotações (opcional)") },
                                enabled = !state.saving, minLines = 3, isError = state.notesError != null,
                                supportingText = { Text(state.notesError ?: "${state.notes.length}/1000 caracteres") }, modifier = Modifier.fillMaxWidth())
                        }
                        Button(onClick = onSave, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(if (state.saving) "Salvando…" else "Salvar acompanhamento")
                        }
                        if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("As alterações são gravadas ao tocar em Salvar acompanhamento.", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { confirmRemoval = true }, enabled = !state.saving,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Remover da coleção") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageBar(title: String, onBack: () -> Unit, enabled: Boolean) {
    TopAppBar(title = { Text(title) }, navigationIcon = {
        TextButton(onClick = onBack, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) { Text("Voltar") }
    })
}

@Composable
private fun InputField(label: String, value: String, onChange: (String) -> Unit,
    error: String? = null, numeric: Boolean = false, enabled: Boolean = true) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        enabled = enabled, singleLine = true, isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        supportingText = if (error != null) ({ ErrorText(error) }) else null,
        modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun MediaCover(item: MediaItem, modifier: Modifier) {
    val resource = when (item.id) {
        "book-lotr" -> R.drawable.lotr_book_cover
        "movie-blade-runner" -> R.drawable.blade_runner
        else -> null
    }
    if (resource != null) Image(painterResource(resource), contentDescription = "Capa de ${item.title}",
        contentScale = ContentScale.Crop, modifier = modifier.clip(RoundedCornerShape(8.dp)))
    else Box(modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
        Text(item.kind.label, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LoadingPanel() {
    Column(Modifier.fillMaxWidth().padding(32.dp).semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = "Carregando coleção" })
        Text("Carregando…")
    }
}

@Composable
private fun MessagePanel(title: String, message: String, actionLabel: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(message)
        Button(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(actionLabel) }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}
