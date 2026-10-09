package com.betopompolo.mediatracker.data

import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind

// Dados simulados compartilhados pelo catalogo XML e pela primeira abertura da colecao.
object MockCatalog {
    val items = listOf(
        MediaItem("book-lotr", "O Senhor dos Anéis: A Sociedade do Anel", MediaKind.BOOK,
            totalUnits = 576, creator = "J. R. R. Tolkien", year = 1954,
            synopsis = "Frodo recebe a missão de levar o Um Anel para longe do Condado. Uma sociedade se forma para acompanhá-lo na jornada pela Terra-média."),
        MediaItem("movie-blade-runner", "Blade Runner 2049", MediaKind.MOVIE,
            totalUnits = 164, creator = "Denis Villeneuve", year = 2017,
            synopsis = "O agente K descobre um segredo que o leva a procurar Rick Deckard, desaparecido há décadas."),
        MediaItem("book-dom-casmurro", "Dom Casmurro", MediaKind.BOOK,
            totalUnits = 256, creator = "Machado de Assis", year = 1899,
            synopsis = "Bentinho revisita sua juventude e sua relação com Capitu, entre memórias, dúvidas e ciúmes."),
        MediaItem("movie-independent", "Filme independente", MediaKind.MOVIE,
            totalUnits = 90)
    )
    fun find(id: String?): MediaItem? = items.firstOrNull { it.id == id }
}
