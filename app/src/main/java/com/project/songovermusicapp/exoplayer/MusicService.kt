package com.project.songovermusicapp.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerListener: MusicPlayerListener

    private var curPlayingSong: MediaMetadataCompat? = null
    private var curPlayingSource: String? = null
    private lateinit var curMediaSource: MediaSource

    private var isPlayerInitialized = false

    private lateinit var firebaseMusicPreparer: MusicPlaybackPreparer
    private lateinit var localMusicPreparer: MusicPlaybackPreparer

    var isForegroundService = false


    override fun onCreate() {
        super.onCreate()
        initLocalPreparer()
        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
            //setCallback(MediaSessionCallback())
        }
        val playBackState = PlaybackStateCompat.Builder().apply {
            setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        }.build()
        mediaSession.setPlaybackState(playBackState)

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
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(localMusicSource))

        musicPlayerListener = MusicPlayerListener(this)
        exoPlayer.addListener(musicPlayerListener)

        musicNotificationManager.showNotification(exoPlayer)

    }

    fun initFirebasePreparer(){
        firebaseMusicPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            //колбэк когда выбран новый трек
            curPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                firebaseMusicSource.asMediaSource(dataSourceFactory),
                it,
                true
            )
            //mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(curMediaSource))

        }
        serviceScope.launch {
            firebaseMusicSource.fetchMedia()
        }

    }

    fun initLocalPreparer(){
        localMusicPreparer = MusicPlaybackPreparer(localMusicSource) {
            //колбэк когда выбран новый трек
            curPlayingSong = it
            preparePlayer(
                localMusicSource.songs,
                localMusicSource.asMediaSource(dataSourceFactory),
                it,
                true
            )
            //mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(curMediaSource))
        }

        serviceScope.launch {
            localMusicSource.fetchMedia()
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        source: ConcatenatingMediaSource,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        Timber.tag("Music Service").d(" Service started at tread: ${Thread.currentThread()}")
        val currentSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
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
                        mediaSessionConnector.setQueueNavigator(
                            MusicQueueNavigator(
                                firebaseMusicSource
                            )
                        )
                        if (curPlayingSource != parentId && firebaseMusicSource.songs.isNotEmpty()) {
                            mediaSessionConnector.setPlaybackPreparer(firebaseMusicPreparer)
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.asMediaSource(dataSourceFactory),
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                            curPlayingSource = parentId
                        }
                        mediaSessionConnector.invalidateMediaSessionQueue()
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
                        mediaSessionConnector.setQueueNavigator(
                            MusicQueueNavigator(
                                localMusicSource
                            )
                        )
                        if (curPlayingSource != parentId && localMusicSource.songs.isNotEmpty()) {
                            mediaSessionConnector.setPlaybackPreparer(localMusicPreparer)
                            preparePlayer(
                                localMusicSource.songs,
                                localMusicSource.asMediaSource(dataSourceFactory),
                                localMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                            curPlayingSource = parentId
                        }
                        mediaSessionConnector.invalidateMediaSessionQueue()
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

        if (curPlayingSource == null && !isPlayerInitialized) {
            curPlayingSource = parentId
        }

    }


//    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
//        override fun onCustomAction(action: String?, extras: Bundle?) {
//            action?.let {
//                when (action) {
//                    CUSTOM_ACTION_PREPARE -> {
//                        preparePlayer(
//                            curMediaSource.songs,
//                            curMediaSource.asMediaSource(dataSourceFactory),
//                            curMediaSource.songs[extras?.getInt("position", 0)?: 0],
//                            playNow = true
//                        )
//                    }
//                }
//            }
//        }
//    }


    private inner class MusicQueueNavigator(val mediaSource: MediaSource) :
        TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            //вызывается сервисом когда нужен новый description для нового трека
            return mediaSource.songs[windowIndex].description
        }
    }


}