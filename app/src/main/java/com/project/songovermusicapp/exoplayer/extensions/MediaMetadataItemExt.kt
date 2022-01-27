package com.project.songovermusicapp.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.project.songovermusicapp.data.entities.Song

fun MediaMetadataCompat.toSong() = description?.let {
    Song(
        it.mediaId.toString(), it.title.toString(),
        it.subtitle.toString(), it.mediaUri.toString(), it.iconUri.toString()
    )
}