package com.project.songovermusicapp.exoplayer.source

import android.support.v4.media.MediaBrowserCompat
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

interface MediaSource {
    suspend fun fetchMedia()
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource
    fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem>
    fun whenReady(action: (Boolean) -> Unit): Boolean
}