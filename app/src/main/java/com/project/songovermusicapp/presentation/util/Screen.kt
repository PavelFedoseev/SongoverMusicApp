package com.project.songovermusicapp.presentation.util

sealed class Screen(val route : String) {
    object MusicListScreen : Screen("music_list_screen")
    object MusicItemScreen : Screen("music_item_screen")

}