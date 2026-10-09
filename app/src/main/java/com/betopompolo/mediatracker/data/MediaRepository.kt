package com.betopompolo.mediatracker.data

import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    val items: Flow<List<MediaItem>>
    suspend fun initialize()
    suspend fun add(draft: MediaDraft): String
    suspend fun updateTracking(id: String, progress: Int, rating: Int?, notes: String)
    suspend fun remove(id: String)
}
