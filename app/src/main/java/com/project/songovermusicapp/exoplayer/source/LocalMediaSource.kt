package com.project.songovermusicapp.exoplayer.source

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.project.songovermusicapp.exoplayer.source.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaSource constructor() : MediaSource {

    var songs = emptyList<MediaMetadataCompat>()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    override suspend fun fetchMedia() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING

    }

    override fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        TODO("Not yet implemented")
    }

    override fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem> {
        TODO("Not yet implemented")
    }

    override fun whenReady(action: (Boolean) -> Unit): Boolean {
        TODO("Not yet implemented")
    }
}