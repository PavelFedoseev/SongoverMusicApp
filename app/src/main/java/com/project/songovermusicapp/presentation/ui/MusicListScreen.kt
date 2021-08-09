package com.project.songovermusicapp.presentation.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Resource
import com.project.songovermusicapp.data.other.Status
import com.project.songovermusicapp.presentation.ui.theme.SongImageSize
import com.project.songovermusicapp.presentation.ui.theme.SongPadding
import com.skydoves.landscapist.CircularRevealedImage
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.glide.GlideImageState
import timber.log.Timber
import kotlin.math.absoluteValue

@Composable
fun LiveDataComponent(
    mediaItemLiveData: LiveData<Resource<List<Song>>>,
    listener: OnItemClickListener
) {
    Timber.tag("Music Service").d(" Service started at tread: ${Thread.currentThread()}")
    val resource by mediaItemLiveData.observeAsState()

    when (resource?.status) {
        Status.SUCCESS -> {
            resource?.data?.let {
                MusicListScreen(list = it, listener)
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
    list: List<Song>,
    listener: OnItemClickListener
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = list, itemContent = { song: Song ->
            SongItem(song = song, listener)
        })
    }
}

@Composable
fun MusicProgressBar() {

}

@Composable
fun SongItem(song: Song, listener: OnItemClickListener) {
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }
    Box(modifier = Modifier.clickable {
        listener.onItemClick(song)
    }) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            dominantColor,
                            defaultDominantColor
                        ),
                        endX = 700f
                    )
                )
                .fillMaxSize()

        )
        {
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
                    elevation = 8.dp
                ) {
                    GlideImage(
                        modifier = Modifier
                            .width(SongImageSize)
                            .height(SongImageSize),
                        imageModel = song.imageUrl,
                        loading = {
                            ConstraintLayout(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val indicator = createRef()
                                CircularProgressIndicator(
                                    modifier = Modifier.constrainAs(indicator) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    }
                                )
                            }
                        },
                        alignment = Alignment.Center,
                        success = { imageState ->
                            calcDominantColor(
                                BitmapDrawable(
                                    LocalContext.current.resources,
                                    imageState.imageBitmap?.asAndroidBitmap()
                                )
                            ) {
                                dominantColor = it
                            }
                            imageState.imageBitmap?.let {
                                CircularRevealedImage(
                                    bitmap = it,
                                    contentDescription = "Music Image",
                                    circularRevealedEnabled = true,
                                    circularRevealedDuration = 1000,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        },
                        failure = {

                        }
                    )
                }
                Spacer(modifier = Modifier.width(30.dp))
                Column {
                    Text(text = song.title, fontSize = 17.sp)
                    Text(text = song.subtitle, fontSize = 13.sp)
                }
            }
        }


    }
}

interface OnItemClickListener {
    fun onItemClick(song: Song)
}


fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
    val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)


    Palette.from(bmp).generate { palette ->
        palette?.dominantSwatch?.rgb?.let { colorValue ->
            var value = Color(colorValue)
            value = Color(red = value.red, green = value.green, blue = value.blue, alpha = 0.6f)
            onFinish(value)
        }
    }
}
