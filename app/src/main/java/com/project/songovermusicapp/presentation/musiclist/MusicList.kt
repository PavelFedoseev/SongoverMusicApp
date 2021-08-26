package com.project.songovermusicapp.presentation.musiclist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.project.songovermusicapp.R
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.presentation.ui.theme.*
import com.project.songovermusicapp.presentation.ui.viewmodel.SongItem
import com.project.songovermusicapp.presentation.ui.viewmodel.SongState
import com.project.songovermusicapp.presentation.util.OnDominantColorListener
import com.project.songovermusicapp.presentation.util.calcDominantColor
import com.skydoves.landscapist.CircularRevealedImage
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun MusicList(
    list: List<Song>,
    itemClickListener: OnItemClickListener,
    curPlayingSongItem: SongItem?
) {
    LazyColumn(modifier = Modifier ) {
        items(items = list, itemContent = { song: Song ->
            var songState = SongState.STOPPED
            if(curPlayingSongItem?.song?.mediaId == song.mediaId)
                songState = curPlayingSongItem.songState
            SongItem(song = song, songPosition = list.indexOf(song), itemClickListener, songState)
        })
    }
}

@Composable
fun MusicProgressBar() {

}

@Composable
fun SongItem(song: Song, songPosition: Int, itemClickListener: OnItemClickListener, songState : SongState = SongState.STOPPED) {


    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }
    val itemColor by animateColorAsState(targetValue = dominantColor)
    Box(modifier = Modifier.clickable {
        itemClickListener.onItemClickPlay(song, songPosition)
    }) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            itemColor,
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
                    shape = RoundedCornerShape(SongImageRoundness),
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
                            imageState.imageBitmap?.let {
                                calcDominantColor(
                                    BitmapDrawable(
                                        LocalContext.current.resources,
                                        Bitmap.createScaledBitmap(
                                            it.asAndroidBitmap(),
                                            20, 20,
                                            false
                                        )
                                    )
                                ) {
                                    dominantColor = it
                                }
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
                    if(songState == SongState.PLAYING){
                        Image(painter = painterResource(id = R.drawable.ic_pause), contentDescription = stringResource(
                            id = R.string.cd_pause_button
                        ))
                    }
                    else if(songState == SongState.PAUSED)
                        Image(painter = painterResource(id = R.drawable.ic_play), contentDescription = stringResource(
                            id = R.string.cd_play_button
                        ))
                }
                Spacer(modifier = Modifier.width(30.dp))
                Column {
                    Text(text = song.title, fontSize = SongItemTextTitle)
                    Text(text = song.subtitle, fontSize = SongItemTextSubtitle)
                }
            }
        }


    }
}

interface OnItemClickListener {
    fun onItemClickPlay(song: Song, position: Int = -1)
    fun onItemClickNext()
    fun onItemClickPrevious()
}

