package com.betopompolo.mediatracker

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.betopompolo.mediatracker.data.LocalMediaRepository
import com.betopompolo.mediatracker.model.MediaDraft
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalMediaRepositoryTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun registrationAndTrackingSurviveStoreRecreation() = runBlocking {
        val file = temp.root.resolve("library.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val first = LocalMediaRepository(PreferenceDataStoreFactory.create(scope = firstScope) { file })
        first.initialize()
        val id = first.add(MediaDraft("Livro persistido", totalUnits = "100"))
        first.updateTracking(id, 35, 4, "  Minha anotação  ")
        firstScope.coroutineContext.job.cancelAndJoin()
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val second = LocalMediaRepository(PreferenceDataStoreFactory.create(scope = secondScope) { file })
            second.initialize()
            val restored = second.items.first().single { it.id == id }
            assertEquals(35, restored.currentUnits)
            assertEquals(4, restored.rating)
            assertEquals("Minha anotação", restored.notes)
            assertNull(restored.creator)
        } finally { secondScope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun deletingAllItemsDoesNotReseedTheLibrary() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repo = LocalMediaRepository(PreferenceDataStoreFactory.create(scope = scope) { temp.root.resolve("empty.preferences_pb") })
            repo.initialize()
            repo.items.first().forEach { repo.remove(it.id) }
            repo.initialize()
            assertTrue(repo.items.first().isEmpty())
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun simultaneousRegistrationsDoNotOverwriteEachOther() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repo = LocalMediaRepository(PreferenceDataStoreFactory.create(scope = scope) { temp.root.resolve("concurrent.preferences_pb") })
            repo.initialize()
            val ids = coroutineScope { (1..12).map { async { repo.add(MediaDraft("Livro $it", totalUnits = "20")) } }.awaitAll() }
            val persisted = repo.items.first().map { it.id }
            assertTrue(persisted.containsAll(ids))
            assertEquals(ids.size, ids.distinct().size)
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun invalidStoredJsonRaisesAnErrorWithoutErasingData() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope) { temp.root.resolve("bad.preferences_pb") }
            store.edit { it[stringPreferencesKey("library_v1")] = "{invalid}" }
            val repo = LocalMediaRepository(store)
            repo.initialize()
            try { repo.items.first(); fail("Esperava erro de leitura") } catch (_: IOException) { }
            assertEquals("{invalid}", store.data.first()[stringPreferencesKey("library_v1")])
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }
}
