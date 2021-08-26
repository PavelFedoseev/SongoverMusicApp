package com.project.songovermusicapp.presentation

import com.project.songovermusicapp.data.entities.Song

interface OnPlayerController {
    fun skipToPrevious()
    fun toggleMusic(mediaItem: Song, toggle: Boolean = false, prepare: Boolean = false)
    fun skipToNext()
    fun seekTo(position: Long)
}