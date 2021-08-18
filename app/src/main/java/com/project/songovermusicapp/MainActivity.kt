package com.project.songovermusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.presentation.MainContent
import com.project.songovermusicapp.presentation.musicitem.MusicItemScreen
import com.project.songovermusicapp.presentation.musiclist.OnItemClickListener
import com.project.songovermusicapp.presentation.splash.SplashScreen
import com.project.songovermusicapp.presentation.ui.theme.SongoverMusicAppTheme
import com.project.songovermusicapp.presentation.ui.viewmodel.MainViewModel
import com.project.songovermusicapp.presentation.util.OnDominantColorListener
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalPagerApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewState by mainViewModel.state.collectAsState()

            val surfaceColor = MaterialTheme.colors.onSurface
            var dominantColor by remember { mutableStateOf(surfaceColor) }

            val animateDominantColor by animateColorAsState(
                targetValue = dominantColor, animationSpec = tween(durationMillis = 1000)
            )

            val dominantColorListener: OnDominantColorListener = object : OnDominantColorListener {
                override fun calculated(color: Color) {
                    dominantColor = color
                }
            }

            val mediaListClickListener = object : OnItemClickListener {
                override fun onItemClickPlay(song: Song) {
                    mainViewModel.onItemSongSelected(song, true)
                }

                override fun onItemClickNext() {
                    mainViewModel.skipToNextSong()
                }

                override fun onItemClickPrevious() {
                    mainViewModel.skipToPreviousSong()
                }
            }
            SongoverMusicAppTheme {
                val navController = rememberNavController()

                val destinationChangeListener =
                    NavController.OnDestinationChangedListener { _, destination, _ ->
                        if(destination.route == "splash_screen"){
                            this@MainActivity.finish()
                        }
                    }
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash_screen",
                        modifier = Modifier.fillMaxSize(),

                    ) {
                        composable("splash_screen") {
                            SplashScreen(navController)
                        }
                        composable("main_screen") {
                            navController.addOnDestinationChangedListener(destinationChangeListener)
                            MainContent(
                                resource = mainViewModel.mediaItems,
                                curPlayingSong = mainViewModel.curPlayingSong,
                                playbackStateCompat = mainViewModel.playbackState,
                                mediaListClickListener = mediaListClickListener,
                                dominantColorListener = dominantColorListener,
                                dominantColor = animateDominantColor,
                                mainCategories = viewState.mainCategories,
                                selectedMainCategory = viewState.selectedCategory,
                                onCategorySelected = mainViewModel::onMainCategorySelected,
                                modifier = Modifier.fillMaxSize(),
                                navController = navController
                            )
                        }
                        composable("music_item_screen") {
                            MusicItemScreen(
                                viewModel = mainViewModel,
                                dominantColorListener = dominantColorListener,
                                dominantColor = animateDominantColor
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        viewModelStore.clear()
        super.onDestroy()
    }
}



/** Composable preview*/
@ExperimentalPagerApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SongoverMusicAppTheme {
//        val song = Song("1", "Too Close", "Alex Clare", "", "")
//        val songItem = SongItem.playing(song)
//        val songItems = listOf(song)

    }
}

