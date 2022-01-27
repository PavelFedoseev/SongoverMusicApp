package com.project.songovermusicapp.exoplayer.extensions

import android.support.v4.media.session.MediaSessionCompat
import com.project.songovermusicapp.data.entities.Song

fun MediaSessionCompat.QueueItem.toSong() = description?.let {
    Song(
        it.mediaId.toString(), it.title.toString(),
        it.subtitle.toString(), it.mediaUri.toString(), it.iconUri.toString()
    )
}