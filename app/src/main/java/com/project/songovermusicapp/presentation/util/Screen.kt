package com.project.songovermusicapp.presentation.util

sealed class Screen(val route : String) {
    object MainScreen : Screen("music_main_screen")
    object MusicItemScreen : Screen("music_item_screen")
}