package com.betopompolo.mediatracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaValidator
import com.betopompolo.mediatracker.ui.theme.MediaTrackerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

private class MemoryRepository : MediaRepository {
    private val data = MutableStateFlow<List<MediaItem>>(emptyList())
    override val items: Flow<List<MediaItem>> = data
    override suspend fun initialize() = Unit
    override suspend fun add(draft: MediaDraft): String {
        val id = "item-${data.value.size}"
        data.value += MediaValidator.toItem(id, draft)
        return id
    }
    override suspend fun updateTracking(id: String, progress: Int, rating: Int?, notes: String) {
        data.value = data.value.map { if (it.id == id) it.copy(currentUnits = progress, rating = rating, notes = notes) else it }
    }
    override suspend fun remove(id: String) { data.value = data.value.filterNot { it.id == id } }
}

class MediaComposeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyLibraryAndInvalidFormGiveFeedback() {
        compose.setContent { MediaTrackerTheme { MediaTrackerApp(MemoryRepository(), {}) } }
        compose.onNodeWithText("Sua coleção está vazia").assertIsDisplayed()
        compose.onNodeWithText("Adicionar primeiro item").performClick()
        compose.onNodeWithText("Salvar na coleção").performScrollTo().performClick()
        compose.onNodeWithText("Use um título com 2 a 120 caracteres.").performScrollTo().assertIsDisplayed()
    }

    @Test fun registrationNavigatesToDetailsAndBackToUpdatedLibrary() {
        compose.setContent { MediaTrackerTheme { MediaTrackerApp(MemoryRepository(), {}) } }
        compose.onNodeWithText("Adicionar primeiro item").performClick()
        compose.onNodeWithText("Título *").performTextInput("Meu livro de teste")
        compose.onNodeWithText("Total de páginas *").performTextInput("200")
        compose.onNodeWithText("Salvar na coleção").performScrollTo().performClick()
        compose.onNodeWithText("Meu livro de teste").assertIsDisplayed()
        compose.onNodeWithText("Marcar concluído").performScrollTo().performClick()
        compose.onNodeWithText("Salvar acompanhamento").performScrollTo().performClick()
        compose.onNodeWithText("Concluído").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Voltar").performClick()
        compose.onNodeWithText("Meu livro de teste").assertIsDisplayed()
        compose.onNodeWithText("Concluído").assertIsDisplayed()
    }
}
