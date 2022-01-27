package com.project.songovermusicapp.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.project.songovermusicapp.R
import com.project.songovermusicapp.exoplayer.MusicService

class MusicPlayerListener(
    private val musicService: MusicService
) : Player.EventListener {
    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, musicService.getText(R.string.toast_player_error), Toast.LENGTH_LONG ).show()
    }

    /**
     * Player.EventListener deprecated
     * методы для Player.Listener
     *
    override fun onPlaybackStateChanged(state: Int) {
    super.onPlaybackStateChanged(state)
    if(state == Player.STATE_READY && musicService.isForegroundService)
    musicService.stopForeground(false)
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
    super.onPlayWhenReadyChanged(playWhenReady, reason)
    if(!playWhenReady && musicService.isForegroundService)
    musicService.stopForeground(false)
    }

     */


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady)
            musicService.stopForeground(false)
    }

}