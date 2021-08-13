package com.project.songovermusicapp.presentation.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.songovermusicapp.data.constants.Constants
import com.project.songovermusicapp.data.constants.Constants.MEDIA_ROOT_ID
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Resource
import com.project.songovermusicapp.exoplayer.MusicService
import com.project.songovermusicapp.exoplayer.MusicServiceConnection
import com.project.songovermusicapp.exoplayer.extensions.currentPlaybackPosition
import com.project.songovermusicapp.exoplayer.extensions.isPlayEnabled
import com.project.songovermusicapp.exoplayer.extensions.isPlaying
import com.project.songovermusicapp.exoplayer.extensions.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    // Выбранная категория
    private val selectedCategory = MutableStateFlow(MainCategory.Remote)
    // Доступные категории
    private val categories = MutableStateFlow(MainCategory.values().asList())

    //State holder of MainActivity
    private val _state = MutableStateFlow(MainViewState())

    val state: StateFlow<MainViewState>
        get() = _state

    private val refreshing = MutableStateFlow(false)

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition


    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState

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
                    errorMessage = null /* TODO */
                )
            }.catch { throwable ->
                // TODO: emit a UI error here. For now we'll just rethrow
                throw throwable
            }.collect{
                _state.value = it
            }
        }
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
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
                    _mediaItems.postValue(Resource.success(itemList))
                }
            })
        updateCurrentPlayerPosition()
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

    /** Функция переключателя контента */
    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID))
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }

    }

    fun onItemSongSelected(mediaItem: Song, toggle: Boolean){
        playOrToggleSong(mediaItem, toggle)
    }

    fun onMainCategorySelected(category: MainCategory){
        selectedCategory.value = category
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }



    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition?:0
                if(curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.currentSongDuration)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

}
enum class MainCategory{
    Local, Remote
}

data class MainViewState(
    val refreshing: Boolean = false,
    val selectedCategory: MainCategory = MainCategory.Remote,
    val mainCategories: List<MainCategory> = emptyList(),
    val errorMessage: String? = null
)

data class SongItem(val songState: SongState, val song: Song? = null){
    companion object{
        fun playing(song: Song) = SongItem(SongState.PLAYING, song)
        fun paused(song: Song) = SongItem(SongState.PAUSED, song)
        fun stopped() = SongItem(SongState.STOPPED)
    }
}

enum class SongState{
    PLAYING,
    PAUSED,
    STOPPED
}