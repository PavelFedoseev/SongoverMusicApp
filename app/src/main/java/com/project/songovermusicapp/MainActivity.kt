package com.project.songovermusicapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.presentation.MainContent
import com.project.songovermusicapp.presentation.OnPlayerController
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

    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private var readPermissionGranted = false
    private var writePermissionGranted = false

    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val viewState by mainViewModel.state.collectAsState()
            val curPlayingSong by mainViewModel.curPlayingSong.observeAsState()
            val playbackState by mainViewModel.playbackState.observeAsState()
            val curSongPosition by mainViewModel.curPlayerPosition.observeAsState()
            val curSongDuration by mainViewModel.curSongDuration.observeAsState()
            val resourceResourceState by mainViewModel.resourceMediaItems.observeAsState()
            val curSongQueue by mainViewModel.curQueue.observeAsState()
            val isShuffle by mainViewModel.isShuffle.observeAsState()
            val isRepeat by mainViewModel.isRepeat.observeAsState()

            val orientation = LocalConfiguration.current.orientation

            val controller = object: OnPlayerController{
                override fun skipToPrevious() {
                    mainViewModel.skipToPreviousSong()
                }

                override fun toggleMusic(mediaItem: Song, toggle: Boolean, prepare: Boolean) {
                    mainViewModel.playOrToggleSong(mediaItem, toggle, prepare)
                }

                override fun skipToNext() {
                    mainViewModel.skipToNextSong()
                }

                override fun seekTo(position: Long) {
                    mainViewModel.seekTo(position)
                }

                override fun toggleShuffle() {
                    mainViewModel.shuffleToggle()
                }

                override fun toggleRepeat() {
                    mainViewModel.repeatToggle()
                }
            }
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
                override fun onItemClickPlay(song: Song, position: Int) {
                    mainViewModel.onItemSongSelected(song, true, position)
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
                        /** NavHost startDestination определяется из BuildConfig */
                        startDestination = "splash_screen",
                        modifier = Modifier.fillMaxSize(),
                        ) {
                        composable("splash_screen") {
                            SplashScreen(navController, mainViewModel)
                        }
                        composable("main_screen") {
                            navController.addOnDestinationChangedListener(destinationChangeListener)
                            MainContent(
                                mediaListClickListener = mediaListClickListener,
                                dominantColorListener = dominantColorListener,
                                dominantColor = animateDominantColor,
                                mainCategories = viewState.mainCategories,
                                selectedMainCategory = viewState.selectedCategory,
                                onCategorySelected = mainViewModel::onMainCategorySelected,
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                curPlayingSong = curPlayingSong,
                                playbackState = playbackState,
                                resourceMediaItems = resourceResourceState,
                                curSongQueue = curSongQueue,
                                isShuffle = isShuffle?: false,
                                isRepeat = isRepeat?: false
                            )
                        }
                        composable("music_item_screen") {
                            MusicItemScreen(
                                dominantColorListener = dominantColorListener,
                                dominantColor = animateDominantColor,
                                curPlayingSong = curPlayingSong,
                                playbackState = playbackState,
                                curSongPosition = curSongPosition,
                                curSongDuration = curSongDuration,
                                controller = controller,
                                isShuffle = isShuffle?: false,
                                isRepeat = isRepeat?: false,
                                orientation = orientation
                            )
                        }
                    }
                }
            }
        }
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if(readPermissionGranted) {
                mainViewModel.onPermissionGranted(true)
            } else {
                Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG).show()
            }
        }
        updateOrRequestPermissions()
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
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

