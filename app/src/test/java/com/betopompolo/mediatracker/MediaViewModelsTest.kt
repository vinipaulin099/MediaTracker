package com.betopompolo.mediatracker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind
import com.betopompolo.mediatracker.model.MediaValidator
import com.betopompolo.mediatracker.ui.state.DetailViewModel
import com.betopompolo.mediatracker.ui.state.FormViewModel
import com.betopompolo.mediatracker.ui.state.LibraryViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeRepository : MediaRepository {
    val data = MutableStateFlow(listOf(MediaItem("one", "Meu livro", MediaKind.BOOK, totalUnits = 100)))
    var failRead = false
    var failWrite = false
    var writes = 0
    override val items: Flow<List<MediaItem>> get() = flow {
        if (failRead) throw IOException("Falha simulada")
        emitAll(data)
    }
    override suspend fun initialize() { delay(1) }
    override suspend fun add(draft: MediaDraft): String {
        delay(10)
        if (failWrite) throw IOException("Falha simulada")
        val id = "new-${++writes}"
        data.value += MediaValidator.toItem(id, draft)
        return id
    }
    override suspend fun updateTracking(id: String, progress: Int, rating: Int?, notes: String) {
        if (failWrite) throw IOException("Falha simulada")
        writes++
        data.value = data.value.map { if (it.id == id) it.copy(currentUnits = progress, rating = rating, notes = notes) else it }
    }
    override suspend fun remove(id: String) {
        if (failWrite) throw IOException("Falha simulada")
        data.value = data.value.filterNot { it.id == id }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelsTest {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { stores.forEach { it.clear() }; stores.clear(); Dispatchers.resetMain() }
    private fun <T : ViewModel> retain(vm: T): T {
        stores += ViewModelStore().apply { put("vm", vm) }
        return vm
    }

    @Test fun loadingContentAndEmptyAreObservable() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(LibraryViewModel(repo, SavedStateHandle()))
        assertTrue(vm.state.value.loading)
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertEquals(1, vm.state.value.visibleItems.size)
        repo.data.value = emptyList()
        advanceUntilIdle()
        assertTrue(vm.state.value.visibleItems.isEmpty())
        assertNull(vm.state.value.error)
    }
    @Test fun failedReadCanBeRetried() = runTest(dispatcher) {
        val repo = FakeRepository().apply { failRead = true }
        val vm = retain(LibraryViewModel(repo, SavedStateHandle()))
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        repo.failRead = false
        vm.retry()
        advanceUntilIdle()
        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.items.size)
    }
    @Test fun searchAndFilterSurviveSavedStateRecreation() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val repo = FakeRepository()
        val vm = retain(LibraryViewModel(repo, saved))
        vm.search("Meu")
        vm.filter(MediaKind.BOOK)
        val restored = retain(LibraryViewModel(repo, SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })))
        advanceUntilIdle()
        assertEquals("Meu", restored.state.value.query)
        assertEquals(MediaKind.BOOK, restored.state.value.kind)
        assertEquals(1, restored.state.value.visibleItems.size)
    }
    @Test fun invalidFormNeverWrites() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(FormViewModel(repo, SavedStateHandle()))
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.state.value.errors.hasErrors)
        assertEquals(0, repo.writes)
    }
    @Test fun validFormIsSavedOnceEvenOnDoubleTap() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(FormViewModel(repo, SavedStateHandle()))
        vm.change(MediaDraft("Meu filme", MediaKind.MOVIE, "90"))
        vm.submit()
        vm.submit()
        assertTrue(vm.state.value.saving)
        advanceUntilIdle()
        assertEquals(1, repo.writes)
        assertNotNull(vm.state.value.savedId)
    }
    @Test fun writeFailurePreservesFormAndAllowsRetry() = runTest(dispatcher) {
        val repo = FakeRepository().apply { failWrite = true }
        val vm = retain(FormViewModel(repo, SavedStateHandle()))
        vm.change(MediaDraft("Livro novo", totalUnits = "300"))
        vm.submit()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertEquals("Livro novo", vm.state.value.draft.title)
        repo.failWrite = false
        vm.submit()
        advanceUntilIdle()
        assertNotNull(vm.state.value.savedId)
    }
    @Test fun formDraftSurvivesRecreation() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val repo = FakeRepository()
        retain(FormViewModel(repo, saved)).change(MediaDraft("Título digitado", MediaKind.MOVIE, "130", "Diretor", "2020"))
        val restored = retain(FormViewModel(repo, SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })))
        assertEquals("Título digitado", restored.state.value.draft.title)
        assertEquals(MediaKind.MOVIE, restored.state.value.draft.kind)
    }
    @Test fun invalidTrackingDoesNotChangeStoredItem() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(DetailViewModel("one", repo, SavedStateHandle()))
        advanceUntilIdle()
        vm.changeProgress("101")
        vm.changeRating("6")
        vm.save()
        advanceUntilIdle()
        assertNotNull(vm.state.value.progressError)
        assertNotNull(vm.state.value.ratingError)
        assertEquals(0, repo.writes)
    }
    @Test fun validTrackingUpdatesTheReactiveItem() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(DetailViewModel("one", repo, SavedStateHandle()))
        advanceUntilIdle()
        vm.changeProgress("100")
        vm.changeRating("5")
        vm.changeNotes("Concluído")
        vm.save()
        advanceUntilIdle()
        assertEquals(100, vm.state.value.item?.currentUnits)
        assertEquals("Acompanhamento salvo.", vm.state.value.message)
    }
    @Test fun trackingDraftSurvivesRecreation() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val repo = FakeRepository()
        val vm = retain(DetailViewModel("one", repo, saved))
        advanceUntilIdle()
        vm.changeProgress("42")
        vm.changeNotes("Ainda não salvei")
        val restored = retain(DetailViewModel("one", repo, SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })))
        advanceUntilIdle()
        assertEquals("42", restored.state.value.progress)
        assertEquals("Ainda não salvei", restored.state.value.notes)
        assertEquals(0, restored.state.value.item?.currentUnits)
    }
    @Test fun removedOrUnknownItemIsHandled() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = retain(DetailViewModel("unknown", repo, SavedStateHandle()))
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.item)
        assertNull(vm.state.value.error)
    }
    @Test fun clearingViewModelCancelsPendingRegistration() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = FormViewModel(repo, SavedStateHandle())
        val store = ViewModelStore().apply { put("vm", vm) }
        vm.change(MediaDraft("Livro cancelado", totalUnits = "10"))
        vm.submit()
        store.clear()
        advanceUntilIdle()
        assertEquals(0, repo.writes)
    }
}
