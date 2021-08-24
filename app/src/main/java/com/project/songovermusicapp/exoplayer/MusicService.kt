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
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.project.songovermusicapp.data.constants.Constants.EXTERNAL_ERROR_EVENT
import com.project.songovermusicapp.data.constants.Constants.MEDIA_FIREBASE_ID
import com.project.songovermusicapp.data.constants.Constants.MEDIA_LOCAL_ID
import com.project.songovermusicapp.data.constants.Constants.NETWORK_ERROR_EVENT
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlaybackPreparer
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlayerListener
import com.project.songovermusicapp.exoplayer.callbacks.MusicPlayerNotificationListener
import com.project.songovermusicapp.exoplayer.source.FirebaseMusicSource
import com.project.songovermusicapp.exoplayer.source.LocalMusicSource
import com.project.songovermusicapp.exoplayer.source.MediaSource
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

    @Inject
    lateinit var localMusicSource: LocalMusicSource




    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerListener: MusicPlayerListener

    private var currentPlayingSong: MediaMetadataCompat? = null
    private var isPlayerInitialized = false

    private lateinit var firebaseMusicPreparer: MusicPlaybackPreparer
    private lateinit var localMusicPreparer : MusicPlaybackPreparer
    private lateinit var curMediaSource: MediaSource

    var isForegroundService = false


    override fun onCreate() {
        super.onCreate()

        firebaseMusicPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            //колбэк когда выбран новый трек
            currentPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                firebaseMusicSource.asMediaSource(dataSourceFactory),
                it,
                true
            )
        }
        localMusicPreparer = MusicPlaybackPreparer(localMusicSource) {
            //колбэк когда выбран новый трек
            currentPlayingSong = it
            preparePlayer(
                localMusicSource.songs,
                localMusicSource.asMediaSource(dataSourceFactory),
                it,
                true
            )
        }
        serviceScope.launch {
            firebaseMusicSource.fetchMedia()
        }
        serviceScope.launch {
            localMusicSource.fetchMedia()
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
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerListener = MusicPlayerListener(this)
        exoPlayer.addListener(musicPlayerListener)

        musicNotificationManager.showNotification(exoPlayer)

    }


    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        source: ConcatenatingMediaSource,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        Timber.tag("Music Service").d(" Service started at tread: ${Thread.currentThread()}")
        val currentSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
//        exoPlayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare(source)
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
        currentSongDuration = exoPlayer.duration
    }

    private fun stopPlayer() {
        exoPlayer.stop()
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

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_LOCAL_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_FIREBASE_ID -> {
                curMediaSource = firebaseMusicSource
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.asMediaSource(dataSourceFactory),
                                firebaseMusicSource.songs[0],
                                false
                            )
                            mediaSessionConnector.setPlaybackPreparer(firebaseMusicPreparer)
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
            MEDIA_LOCAL_ID -> {
                curMediaSource = localMusicSource
                val resultSent = localMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(localMusicSource.asMediaItems())
                        if (localMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                localMusicSource.songs,
                                localMusicSource.asMediaSource(dataSourceFactory),
                                localMusicSource.songs[0],
                                false
                            )
                            mediaSessionConnector.setPlaybackPreparer(localMusicPreparer)
                            mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(localMusicSource))
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(EXTERNAL_ERROR_EVENT, null)
                        result.sendResult(null)
                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }

        }
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(curMediaSource))
    }



    private inner class MusicQueueNavigator(val mediaSource: MediaSource) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            //вызывается сервисом когда нужен новый description для нового трека
            return mediaSource.songs[windowIndex].description
        }
    }
}