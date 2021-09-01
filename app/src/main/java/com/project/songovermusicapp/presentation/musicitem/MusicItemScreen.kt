package com.project.songovermusicapp.presentation.musicitem

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.insets.statusBarsHeight
import com.project.songovermusicapp.R
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.exoplayer.toSong
import com.project.songovermusicapp.presentation.MusicSeekBar
import com.project.songovermusicapp.presentation.OnPlayerController
import com.project.songovermusicapp.presentation.ui.theme.*
import com.project.songovermusicapp.presentation.ui.viewmodel.SongItem
import com.project.songovermusicapp.presentation.ui.viewmodel.SongState
import com.project.songovermusicapp.presentation.util.OnDominantColorListener
import com.project.songovermusicapp.presentation.util.calcDominantColor
import com.skydoves.landscapist.CircularRevealedImage
import com.skydoves.landscapist.glide.GlideImage

@ExperimentalMaterialApi
@Composable
fun MusicItemScreen(
    dominantColorListener: OnDominantColorListener,
    dominantColor: Color,
    curPlayingSong: MediaMetadataCompat?,
    playbackState: PlaybackStateCompat?,
    curSongPosition: Long?,
    curSongDuration: Long?,
    controller: OnPlayerController,
    isShuffle: Boolean,
    isRepeat: Boolean,
    orientation: Int
) {

    var songItem by remember { mutableStateOf(SongItem.stopped()) }

    curPlayingSong?.toSong()?.let {
        controller.toggleMusic(it, prepare = true)
        when (playbackState?.state) {
            PlaybackStateCompat.STATE_PAUSED -> {
                songItem = SongItem.paused(it)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                songItem = SongItem.playing(it)
            }
            else -> {
            }
        }
    }


    val constraintSet = ConstraintSet {
        val musicTopBar = createRefFor("song_top_bar")
        val musicAlbumImage = createRefFor("album_image")
        val musicTextInfo = createRefFor("song_text")
        val musicControlPanel = createRefFor("song_control_panel")
        val musicBottomBar = createRefFor("song_bottom_bar")

        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                constrain(musicTopBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
                constrain(musicAlbumImage) {
                    start.linkTo(parent.start)
                    top.linkTo(musicTopBar.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                constrain(musicTextInfo) {
                    top.linkTo(musicTopBar.bottom)
                    start.linkTo(musicAlbumImage.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(musicControlPanel.top)
                }
                constrain(musicControlPanel) {
                    start.linkTo(musicAlbumImage.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(musicBottomBar.top)

                }
                constrain(musicBottomBar) {
                    start.linkTo(musicAlbumImage.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            }
            else -> {
                constrain(musicTopBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
                constrain(musicAlbumImage) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(musicTopBar.bottom)
                    bottom.linkTo(musicTextInfo.top)
                    height = Dimension.preferredWrapContent
                }
                constrain(musicTextInfo) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(musicControlPanel.top)
                }
                constrain(musicControlPanel) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(musicBottomBar.top)

                }
                constrain(musicBottomBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            }
        }


    }
    ConstraintLayout(
        constraintSet = constraintSet, modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(dominantColor, Color.Transparent)
                )
            )
    ) {
        Box(
            Modifier
                .layoutId("song_top_bar")
        ) {
            Spacer(
                Modifier
                    .background(dominantColor)
                    .fillMaxWidth()
                    .statusBarsHeight()
            )
        }
        Box(modifier = Modifier.layoutId("album_image")) {
            AlbumMusicImage(song = songItem.song ?: Song(), dominantColorListener)
        }
        Box(modifier = Modifier.layoutId("song_text")) {
            MusicItemText(song = songItem.song ?: Song())
        }
        Box(
            modifier = Modifier.layoutId("song_control_panel")
        ) {
            MusicItemPlayerController(
                songItem = songItem,
                playbackState = playbackState,
                listener = controller,
                curSongPosition = curSongPosition ?: 0L,
                curSongDuration = curSongDuration ?: 0L,
                isShuffle = isShuffle,
                isRepeat = isRepeat
            )
        }
        Box(modifier = Modifier.layoutId("song_bottom_bar")) {
            SongBottomBar()
        }
    }
}

@Composable
private fun SongBottomBar() {
    Spacer(
        modifier = Modifier
            .height(MusicItemScreenBottomBar)
            .fillMaxWidth()
    )
}

@Composable
private fun AlbumMusicImage(song: Song, dominantColorListener: OnDominantColorListener) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .width(MusicItemScreenImageSize)
                .height(MusicItemScreenImageSize)
                .layoutId("imageCard"),
            shape = RoundedCornerShape(MusicItemScreenImageRoundness),
            elevation = 8.dp
        ) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = song.imageUrl,
                contentScale = ContentScale.Crop,
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
                success = { imageState ->
                    imageState.imageBitmap?.let { imageBitmap ->
                        calcDominantColor(
                            BitmapDrawable(
                                LocalContext.current.resources,
                                Bitmap.createScaledBitmap(
                                    imageBitmap.asAndroidBitmap(),
                                    20, 20,
                                    false
                                )
                            )
                        ) {
                            dominantColorListener.calculated(it)
                        }
                        val bitmap = Bitmap.createScaledBitmap(
                            imageBitmap.asAndroidBitmap(),
                            1024, 1024,
                            false
                        )
                        CircularRevealedImage(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Music Image",
                            circularRevealedEnabled = false,
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                alignment = Alignment.Center
            )
        }
    }
}

@Composable
private fun MusicItemText(song: Song) {
    Column(
        modifier = Modifier
            .padding(
                top = MusicItemScreenPadding,
                start = MusicItemScreenPaddingStart,
                end = MusicItemScreenPaddingEnd
            )
            .fillMaxWidth()
    ) {
        Text(
            text = song.title,
            fontSize = MusicItemScreenTitle,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.subtitle,
            fontSize = MusicItemScreenSubtitle,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                vertical = MusicItemScreenPadding
            )
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun MusicItemPlayerController(
    songItem: SongItem,
    playbackState: PlaybackStateCompat?,
    listener: OnPlayerController,
    curSongPosition: Long,
    curSongDuration: Long,
    isShuffle: Boolean,
    isRepeat: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MusicItemScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MusicSeekBar(
            timePosition = curSongPosition,
            duration = curSongDuration,
            modifier = Modifier
                .height(45.dp)
                .padding(bottom = MusicItemScreenPadding)
        ) { position ->
            listener.seekTo(position)
        }
        val constraintSet = ConstraintSet {
            val shuffleCon = createRefFor("shuffleCon")
            val previousCon = createRefFor("previousCon")
            val playCon = createRefFor("playCon")
            val nextCon = createRefFor("nextCon")
            val repeatCon = createRefFor("repeatCon")

            constrain(shuffleCon) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(previousCon.start)
                width =
                    Dimension.value(
                        MusicItemScreenControllerBtnHeight
                                + MusicItemScreenPadding +
                                MusicItemScreenPadding
                    )
            }
            constrain(previousCon) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(playCon.start)
                width =
                    Dimension.value(
                        MusicItemScreenControllerBtnHeight
                                + MusicItemScreenPadding +
                                MusicItemScreenPadding
                    )
            }

            constrain(playCon) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.wrapContent
            }
            constrain(nextCon) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(playCon.end)
                width = Dimension.value(
                    MusicItemScreenControllerBtnHeight
                            + MusicItemScreenPadding +
                            MusicItemScreenPadding
                )
            }
            constrain(repeatCon) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(nextCon.end)
                width = Dimension.value(
                    MusicItemScreenControllerBtnHeight
                            + MusicItemScreenPadding +
                            MusicItemScreenPadding
                )
            }
        }
        ConstraintLayout(modifier = Modifier.fillMaxWidth(), constraintSet = constraintSet) {
            Card(modifier = Modifier.layoutId("shuffleCon")
                .padding(horizontal = MusicItemScreenPadding),
                onClick = {listener.toggleShuffle()}) {
                Image(
                    painter = if (isShuffle) painterResource(id = R.drawable.exo_icon_shuffle_on) else painterResource(
                        id = R.drawable.exo_icon_shuffle_off
                    ),
                    contentDescription = stringResource(
                        id = R.string.cd_shuffle
                    )
                )
            }

            Card(
                modifier = Modifier
                    .layoutId("previousCon")
                    .width(MusicItemScreenControllerBtnHeight)
                    .height(MusicItemScreenControllerBtnHeight)
                    .padding(horizontal = MusicItemScreenPadding),
                shape = RoundedCornerShape(BottomBarPlayerRoundness),
                onClick = { listener.skipToPrevious() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_previous),
                    contentDescription = stringResource(
                        id = R.string.cd_previous
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MusicItemScreenControllerBtnPadding)
                )
            }
            val painter = when (songItem.songState) {
                SongState.PLAYING -> {
                    painterResource(id = R.drawable.ic_pause)
                }
                SongState.PAUSED -> {
                    painterResource(id = R.drawable.ic_play)
                }
                else -> {
                    painterResource(id = R.drawable.ic_play)
                }
            }

            Card(
                modifier = Modifier
                    .layoutId("playCon")
                    .width(MusicItemScreenControllerBtnHeight)
                    .height(MusicItemScreenControllerBtnHeight),
                shape = RoundedCornerShape(BottomBarPlayerRoundness),
                onClick = { songItem.song?.let { listener.toggleMusic(songItem.song, true) } }
            ) {
                Image(
                    painter = painter,
                    contentDescription = stringResource(
                        id = R.string.cd_play_button
                    ),
                    Modifier
                        .fillMaxSize()
                        .padding(MusicItemScreenControllerBtnPadding),
                    contentScale = ContentScale.FillBounds
                )
            }
            Card(
                modifier = Modifier
                    .layoutId("nextCon")
                    .width(MusicItemScreenControllerBtnHeight)
                    .height(MusicItemScreenControllerBtnHeight)
                    .padding(horizontal = MusicItemScreenPadding),
                shape = RoundedCornerShape(BottomBarPlayerRoundness),
                onClick = { listener.skipToNext() }
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_next),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MusicItemScreenControllerBtnPadding),
                    contentDescription = stringResource(
                        id = R.string.cd_next
                    )
                )
            }
            Card(modifier = Modifier.layoutId("repeatCon")
                .padding(horizontal = MusicItemScreenPadding),
                onClick = {listener.toggleRepeat()}) {
                Image(
                    painter = if (isRepeat){ painterResource(id = R.drawable.exo_icon_repeat_one)} else{ painterResource(
                        id = R.drawable.exo_icon_repeat_off
                    )},
                    contentDescription = stringResource(
                        id = R.string.cd_shuffle
                    )
                )
            }
        }
    }
}
