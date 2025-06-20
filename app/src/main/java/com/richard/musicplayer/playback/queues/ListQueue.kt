package com.richard.musicplayer.playback.queues

import com.richard.musicplayer.models.MediaMetadata

class ListQueue(
    override val playlistId: String? = null,
    val title: String? = null,
    val items: List<MediaMetadata>,
    override val startShuffled: Boolean = false,
    val startIndex: Int = 0,
    val position: Long = 0L,
) : Queue {
    override val preloadItem: MediaMetadata? = null
    override suspend fun getInitialStatus() = Queue.Status(title, items, startIndex, position)
    override fun hasNextPage(): Boolean = false
    override suspend fun nextPage() = throw UnsupportedOperationException()
}