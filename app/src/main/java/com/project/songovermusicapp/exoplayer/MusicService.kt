package com.project.songovermusicapp.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.project.songovermusicapp.data.constants.Constants.MEDIA_ROOT_ID
import com.project.songovermusicapp.data.constants.Constants.NETWORK_ERROR_EVENT
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlaybackPreparer
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlayerListener
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlayerNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
    companion object {
        const val TAG = "MusicService"

        var currentSongDuration = 0L
            private set
    }

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerListener: MusicPlayerListener

    private var currentPlayingSong: MediaMetadataCompat? = null
    private var isPlayerInitialized = false

    var isForegroundService = false


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseMusicSource.fetchMedia()
        }
        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) { //изменение длительности песни в нотфикации
            currentSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            //колбэк когда выбран новый трек
            currentPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerListener = MusicPlayerListener(this)
        exoPlayer.addListener(musicPlayerListener)

        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        Timber.tag("Music Service").d(" Service started at tread: ${Thread.currentThread()}")
        val currentSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
//        exoPlayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerListener)
        exoPlayer.release()
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR_EVENT, null)
                        result.sendResult(null)
                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            //вызывается сервисом когда нужен новый description для нового трека
            return firebaseMusicSource.songs[windowIndex].description
        }
    }
}