package com.betopompolo.mediatracker.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaKind(val label: String, val unit: String) {
    BOOK("Livro", "páginas"), MOVIE("Filme", "minutos")
}

@Serializable
data class MediaItem(
    val id: String,
    val title: String,
    val kind: MediaKind,
    val totalUnits: Int,
    val creator: String? = null,
    val year: Int? = null,
    val synopsis: String? = null,
    val currentUnits: Int = 0,
    val rating: Int? = null,
    val notes: String = ""
) {
    init {
        require(id.isNotBlank())
        require(title.trim().length in 2..120)
        require(totalUnits in 1..100_000)
        require(currentUnits in 0..totalUnits)
        require(rating == null || rating in 1..5)
        require(year == null || year in 1500..2100)
        require(creator == null || creator.length <= 100)
        require(notes.length <= 1000)
    }

    val progressPercent: Int get() = (currentUnits.toLong() * 100 / totalUnits).toInt()
    val status: String get() = when (currentUnits) {
        0 -> "Quero começar"
        totalUnits -> "Concluído"
        else -> "Em andamento"
    }
}

data class MediaDraft(
    val title: String = "",
    val kind: MediaKind = MediaKind.BOOK,
    val totalUnits: String = "",
    val creator: String = "",
    val year: String = ""
)

data class FormErrors(
    val title: String? = null,
    val totalUnits: String? = null,
    val creator: String? = null,
    val year: String? = null
) {
    val hasErrors: Boolean get() = listOf(title, totalUnits, creator, year).any { it != null }
}

object MediaValidator {
    fun validate(draft: MediaDraft): FormErrors = FormErrors(
        title = if (draft.title.trim().length !in 2..120) "Use um título com 2 a 120 caracteres." else null,
        totalUnits = if (draft.totalUnits.toIntOrNull() !in 1..100_000)
            "Informe um total entre 1 e 100.000." else null,
        creator = if (draft.creator.trim().length > 100) "Use até 100 caracteres." else null,
        year = if (draft.year.isNotBlank() && draft.year.toIntOrNull() !in 1500..2100)
            "Informe um ano entre 1500 e 2100 ou deixe vazio." else null
    )

    fun toItem(id: String, draft: MediaDraft): MediaItem {
        require(!validate(draft).hasErrors) { "Cadastro inválido." }
        return MediaItem(
            id = id, title = draft.title.trim(), kind = draft.kind,
            totalUnits = draft.totalUnits.toInt(),
            creator = draft.creator.trim().takeIf { it.isNotEmpty() },
            year = draft.year.toIntOrNull()
        )
    }
}
