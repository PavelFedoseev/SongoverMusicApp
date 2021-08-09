package com.project.songovermusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bumptech.glide.RequestManager
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Status
import com.project.songovermusicapp.presentation.ui.LiveDataComponent
import com.project.songovermusicapp.presentation.ui.OnItemClickListener
import com.project.songovermusicapp.presentation.ui.theme.SongoverMusicAppTheme
import com.project.songovermusicapp.presentation.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initObservers()
        setContent {
            SongoverMusicAppTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LiveDataComponent(mainViewModel.mediaItems, object: OnItemClickListener{
                        override fun onItemClick(song: Song) {
                            mainViewModel.playOrToggleSong(song, true)
                        }
                    })
                }
            }
        }
    }

    private fun initObservers(){
        mainViewModel.mediaItems.observe(this){ result ->
            when(result.status){
                Status.SUCCESS ->{
                    result.data
                }
                Status.ERROR -> Unit
                Status.LOADING -> Unit
            }

        }
    }


}





@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SongoverMusicAppTheme {

    }
}