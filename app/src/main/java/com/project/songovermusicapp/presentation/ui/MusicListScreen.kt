package com.project.songovermusicapp.presentation.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Resource
import com.project.songovermusicapp.data.other.Status
import com.project.songovermusicapp.presentation.ui.theme.SongImageSize
import com.project.songovermusicapp.presentation.ui.theme.SongPadding

@Composable
fun LiveDataComponent(mediaItemLiveData: LiveData<Resource<List<Song>>>) {
    val resource by mediaItemLiveData.observeAsState()

    when (resource?.status) {
        Status.SUCCESS -> {
            resource?.data?.let {
                MusicListScreen(list = it)
            }
        }
        Status.ERROR -> {

        }
        Status.LOADING -> {
        MusicProgressBar()
        }
    }
}

@Composable
fun MusicListScreen(
    list: List<Song>
) {
    LazyColumn {
        items(items = list, itemContent = { song: Song ->
            SongItem(song = song)
        })
    }
}

@Composable
fun MusicProgressBar() {

}

@Composable
fun SongItem(song: Song) {
    Row(
        modifier = Modifier
            .padding(SongPadding)
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .width(SongImageSize)
                .height(SongImageSize),
            shape = RoundedCornerShape(5.dp),
            elevation = 5.dp
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_music),
//                contentDescription = "Music Image"
//            )
        }
        Spacer(modifier = Modifier.width(30.dp))
        Column() {
            Text(text = song.title, color = Color.Black, fontSize = 15.sp)
            Text(text = song.subtitle, color = Color.Gray, fontSize = 10.sp)
        }


    }
}

fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
    val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)


//    Palette.from(bmp).generate { palette ->
//        palette?.dominantSwatch?.rgb?.let { colorValue ->
//            onFinish(Color(colorValue))
//        }
//    }
}
