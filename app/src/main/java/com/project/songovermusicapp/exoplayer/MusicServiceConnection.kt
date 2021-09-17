package com.project.songovermusicapp.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.SimpleExoPlayer
import com.project.songovermusicapp.R
import com.project.songovermusicapp.data.constants.Constants.CUSTOM_ACTION_PREPARE
import com.project.songovermusicapp.data.constants.Constants.EXTERNAL_ERROR_EVENT
import com.project.songovermusicapp.data.constants.Constants.NETWORK_ERROR_EVENT
import com.project.songovermusicapp.data.other.Event
import com.project.songovermusicapp.data.other.Resource
import timber.log.Timber
import javax.inject.Inject

class MusicServiceConnection(private val context: Context) {

    companion object{
        private const val TAG = "MusicServiceConnection"
    }

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _currentlyPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentlyPlayingSong: LiveData<MediaMetadataCompat?> = _currentlyPlayingSong

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val _curQueue =
        MutableLiveData(emptyList<MediaSessionCompat.QueueItem>().toMutableList())
    val curQueue: LiveData<MutableList<MediaSessionCompat.QueueItem>> = _curQueue

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    lateinit var mediaController: MediaControllerCompat

    /** Контроллер для получение доступа к взаимодействию с плеером*/
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    /** Подписка на получение объектов медиа по определённому parentId*/
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    /** Отписка от объектов медиа по parentId*/
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun changeLoadChild(parentId: String){
    }



    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(
                    Resource.error(
                        context.getString(R.string.message_connection_suspended),
                        false
                    )
                )
            )
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(
                    Resource.error(
                        context.getString(R.string.message_network_error),
                        false
                    )
                )
            )
        }


    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentlyPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_ERROR_EVENT -> _networkError.postValue(
                    Event(Resource.error(context.getString(R.string.message_network_error), null))
                )
                EXTERNAL_ERROR_EVENT -> _networkError.postValue(
                    Event(Resource.error(context.getString(R.string.message_external_error), null))
                )
            }
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            _curQueue.postValue(queue)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}