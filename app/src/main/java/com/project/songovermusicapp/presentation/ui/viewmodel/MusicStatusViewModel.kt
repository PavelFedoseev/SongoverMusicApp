package com.project.songovermusicapp.presentation.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.songovermusicapp.data.constants.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.exoplayer.MusicService
import com.project.songovermusicapp.exoplayer.MusicServiceConnection
import com.project.songovermusicapp.exoplayer.extensions.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicStatusViewModel @Inject constructor(
musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState


}