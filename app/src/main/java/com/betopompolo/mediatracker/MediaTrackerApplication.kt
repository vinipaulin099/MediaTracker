package com.betopompolo.mediatracker

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.betopompolo.mediatracker.data.LocalMediaRepository
import com.betopompolo.mediatracker.data.MediaRepository

private val Context.libraryDataStore by preferencesDataStore(name = "media_library")

class MediaTrackerApplication : Application() {
    // Uma unica instancia por processo: Activities nunca criam outro DataStore para o mesmo arquivo.
    val repository: MediaRepository by lazy { LocalMediaRepository(libraryDataStore) }
}
