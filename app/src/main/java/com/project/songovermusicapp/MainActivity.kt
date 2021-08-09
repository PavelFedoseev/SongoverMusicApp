package com.project.songovermusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.RequestManager
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Status
import com.project.songovermusicapp.presentation.ui.theme.SongImageSize
import com.project.songovermusicapp.presentation.ui.theme.SongPadding
import com.project.songovermusicapp.presentation.ui.theme.SongoverMusicAppTheme
import com.project.songovermusicapp.presentation.ui.viewmodel.MainViewModel
import com.project.songovermusicapp.presentation.ui.viewmodel.MusicListViewModel
import com.project.songovermusicapp.presentation.util.Navigation
import com.project.songovermusicapp.presentation.util.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SongoverMusicAppTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxSize()
                ) {
//                    val navController = rememberNavController()
//                    NavHost(navController = navController,  startDestination = Screen.MusicListScreen.route){
//                        composable(Screen.MusicListScreen.route){
//
//                        }
//                        composable(Screen.MusicItemScreen.route){
//
//                        }
//                    }
                }
            }
        }
    }

    private fun initObservers(){
        mainViewModel.mediaItems.observe(this){ result ->
            when(result.status){
                Status.SUCCESS ->{


                }
                Status.ERROR -> Unit
                Status.LOADING -> Unit
            }

        }
    }


}




@Composable
fun MusicList(
    viewModel: MusicListViewModel
){


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SongoverMusicAppTheme {
        //SongItem(song = Song(title = "Title", subtitle = "Subtitle"))
    }
}