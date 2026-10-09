package com.betopompolo.mediatracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaValidator
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalMediaRepository(private val store: DataStore<Preferences>) : MediaRepository {
    private val libraryKey = stringPreferencesKey("library_v1")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override val items: Flow<List<MediaItem>> = store.data.map { decode(it[libraryKey]) }

    override suspend fun initialize() {
        store.edit { prefs ->
            // A chave ausente significa primeira abertura; [] e uma colecao vazia valida.
            if (prefs[libraryKey] == null) prefs[libraryKey] = json.encodeToString(MockCatalog.items)
        }
    }

    override suspend fun add(draft: MediaDraft): String {
        val item = MediaValidator.toItem(UUID.randomUUID().toString(), draft)
        mutate { it + item }
        return item.id
    }

    override suspend fun updateTracking(id: String, progress: Int, rating: Int?, notes: String) {
        mutate { list ->
            require(list.any { it.id == id }) { "Item não encontrado." }
            list.map { if (it.id == id) it.copy(currentUnits = progress, rating = rating, notes = notes.trim()) else it }
        }
    }

    override suspend fun remove(id: String) {
        mutate { list -> list.filterNot { it.id == id } }
    }

    private suspend fun mutate(change: (List<MediaItem>) -> List<MediaItem>) {
        // edit serializa as gravacoes; cada alteracao usa a versao mais recente dos dados.
        store.edit { prefs -> prefs[libraryKey] = json.encodeToString(change(decode(prefs[libraryKey]))) }
    }

    private fun decode(encoded: String?): List<MediaItem> {
        if (encoded == null) return emptyList()
        return try {
            json.decodeFromString<List<MediaItem>>(encoded)
        } catch (error: SerializationException) {
            throw IOException("Não foi possível ler a coleção salva.", error)
        } catch (error: IllegalArgumentException) {
            throw IOException("A coleção contém dados inválidos.", error)
        }
    }
}
