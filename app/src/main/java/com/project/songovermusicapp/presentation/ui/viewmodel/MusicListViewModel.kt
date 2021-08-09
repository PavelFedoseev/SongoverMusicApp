package com.project.songovermusicapp.presentation.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.project.songovermusicapp.data.entities.Song

class MusicListViewModel : ViewModel() {
    var musicList = mutableStateOf<List<Song>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)


}