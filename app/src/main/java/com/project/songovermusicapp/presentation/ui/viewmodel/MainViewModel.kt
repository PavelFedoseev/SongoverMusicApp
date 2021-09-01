package com.project.songovermusicapp.presentation.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.songovermusicapp.START_CATEGORY
import com.project.songovermusicapp.START_SOURCE
import com.project.songovermusicapp.data.constants.Constants
import com.project.songovermusicapp.data.constants.Constants.MEDIA_FIREBASE_ID
import com.project.songovermusicapp.data.constants.Constants.MEDIA_LOCAL_ID
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Resource
import com.project.songovermusicapp.data.other.Source
import com.project.songovermusicapp.exoplayer.MusicService
import com.project.songovermusicapp.exoplayer.MusicServiceConnection
import com.project.songovermusicapp.exoplayer.extensions.currentPlaybackPosition
import com.project.songovermusicapp.exoplayer.extensions.isPlayEnabled
import com.project.songovermusicapp.exoplayer.extensions.isPlaying
import com.project.songovermusicapp.exoplayer.extensions.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    // Выбранная категория
    private val selectedCategory = MutableStateFlow(MainCategory.Local)

    // Доступные категории
    private val categories = MutableStateFlow(MainCategory.values().asList())

    //State holder of MainActivity
    private val _state = MutableStateFlow(MainViewState())

    val state: StateFlow<MainViewState>
        get() = _state

    private val refreshing = MutableStateFlow(false)

    private val _isShuffle = MutableLiveData(false)
    val isShuffle: LiveData<Boolean> = _isShuffle

    private val _isRepeat = MutableLiveData(false)
    val isRepeat: LiveData<Boolean> = _isRepeat

    private val _resourceMediaItems = MutableLiveData<Resource<List<Song>>>()
    val resourceMediaItems: LiveData<Resource<List<Song>>> = _resourceMediaItems

    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    private val _curPlayingSource = MutableLiveData<Source>()
    val curPlayingSource: LiveData<Source> = _curPlayingSource


    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState
    val curQueue = musicServiceConnection.curQueue

    init {
        viewModelScope.launch {
            // Combines the latest value from each of the flows, allowing us to generate a
            // view state instance which only contains the latest values.
            combine(
                categories,
                selectedCategory,
                refreshing
            ) { categories, selectedCategory, refreshing ->
                MainViewState(
                    mainCategories = categories,
                    selectedCategory = selectedCategory,
                    refreshing = refreshing,
                    errorMessage = null
                )
            }.catch { throwable ->
                throw throwable
            }.collect {
                _state.value = it
            }
        }
        _resourceMediaItems.value = Resource.loading(null)
        subscribeToSource(START_SOURCE)
        updateCurrentPlayerPosition()
        onMainCategorySelected(category = START_CATEGORY)
    }

    /** Функция перехода к следующему треку */
    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    /** Функция перехода к предыдущему треку */
    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    /** Функция перехода к точке трека */
    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun shuffleToggle() {
        if (isShuffle.value == false) {
            musicServiceConnection.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)
            _isShuffle.postValue(true)
        }
        else {
            musicServiceConnection.transportControls.setShuffleMode(SHUFFLE_MODE_NONE)
            _isShuffle.postValue(false)
        }
    }

    fun repeatToggle(){
        if(isRepeat.value == false) {
            musicServiceConnection.transportControls.setRepeatMode(REPEAT_MODE_ONE)
            _isRepeat.postValue(true)
        }
        else{
            musicServiceConnection.transportControls.setRepeatMode(REPEAT_MODE_NONE)
            _isRepeat.postValue(false)
        }
    }

    /** Функция переключателя контента */
    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false, prepare: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID))
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> if (!prepare) musicServiceConnection.transportControls.play() else musicServiceConnection.transportControls.prepare()
                    else -> Unit
                }
            }
        else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }

    }

    /** Выбран трек из списка или из нижней панели*/
    fun onItemSongSelected(mediaItem: Song, toggle: Boolean, position: Int) {
//        if(position!=-1){
//            musicServiceConnection.preparePlayer(position)
//        }
        playOrToggleSong(mediaItem, toggle)
    }

    fun onMainCategorySelected(category: MainCategory) {
        selectedCategory.value = category
        when (category) {
            MainCategory.Remote -> {
                subscribeToSource(MEDIA_FIREBASE_ID)
            }
            MainCategory.Local -> {
                subscribeToSource(MEDIA_LOCAL_ID)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_FIREBASE_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
        musicServiceConnection.unsubscribe(
            MEDIA_LOCAL_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})

    }


    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition ?: 0
                if (curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.currentSongDuration)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }


    private fun subscribeToSource(parentId: String) {
        //unsubscribeFromSources()
        musicServiceConnection.subscribe(
            parentId,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val itemList = children.map {
                        Song(
                            it.mediaId.toString(),
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }
                    _resourceMediaItems.postValue(Resource.success(itemList))
                }
            })
    }

    private fun unsubscribeFromSource(parentId: String) {
        musicServiceConnection.unsubscribe(
            parentId,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}

enum class MainCategory {
    Local, Remote
}

data class MainViewState(
    val refreshing: Boolean = false,
    val selectedCategory: MainCategory = MainCategory.Remote,
    val mainCategories: List<MainCategory> = emptyList(),
    val errorMessage: String? = null
)

data class SongItem(val songState: SongState, val song: Song? = null) {
    companion object {
        fun playing(song: Song) = SongItem(SongState.PLAYING, song)
        fun paused(song: Song) = SongItem(SongState.PAUSED, song)
        fun stopped() = SongItem(SongState.STOPPED)
    }
}

enum class SongState {
    PLAYING,
    PAUSED,
    STOPPED
}