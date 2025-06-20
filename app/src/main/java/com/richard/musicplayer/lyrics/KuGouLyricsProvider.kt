package com.richard.musicplayer.lyrics

import android.content.Context
import com.zionhuang.kugou.KuGou
import com.richard.musicplayer.constants.EnableKugouKey
import com.richard.musicplayer.utils.dataStore
import com.richard.musicplayer.utils.get

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        KuGou.getAllPossibleLyricsOptions(title, artist, duration, callback)
    }
}
