package com.project.songovermusicapp.presentation

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.project.songovermusicapp.R
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.exoplayer.extensions.toSong
import com.project.songovermusicapp.exoplayer.toSong
import com.project.songovermusicapp.presentation.musiclist.OnItemClickListener
import com.project.songovermusicapp.presentation.ui.theme.*
import com.project.songovermusicapp.presentation.ui.viewmodel.SongItem
import com.project.songovermusicapp.presentation.ui.viewmodel.SongState
import com.project.songovermusicapp.presentation.util.OnDominantColorListener
import com.project.songovermusicapp.presentation.util.calcDominantColor
import com.skydoves.landscapist.CircularRevealedImage
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.collect

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomMusicBar(
    modifier: Modifier = Modifier,
    pagerModifier: Modifier = Modifier,
    curSongMetadata: MediaMetadataCompat?,
    playbackState: PlaybackStateCompat?,
    sessionQueueItems: List<MediaSessionCompat.QueueItem>?,
    onItemClickListener: OnItemClickListener,
    dominantColorListener: OnDominantColorListener,
    navController: NavController,
    isShuffle: Boolean,
    songItems: List<Song>
) {

    var curSongItem by remember { mutableStateOf(SongItem.stopped()) }

    curSongMetadata?.toSong()?.let {
        when (playbackState?.state) {
            PlaybackStateCompat.STATE_PAUSED -> {
                curSongItem = SongItem.paused(it)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                curSongItem = SongItem.playing(it)
            }
            else -> {

            }
        }
    }

    val pagerState = rememberPagerState(
        pageCount = songItems.size,
        initialPage = 0
    )

    var newPageIndex by remember {
        mutableStateOf(0)
    }


    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            when (newPageIndex) {
                page + 1 -> onItemClickListener.onItemClickPrevious()
                page - 1 -> onItemClickListener.onItemClickNext()
            }
        }
        snapshotFlow { pagerState.isScrollInProgress }
    }

    var dominantColor by remember { mutableStateOf(Color.Transparent) }
    val animateDominantColor by animateColorAsState(targetValue = dominantColor)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomBarHeight)
            .background(Brush.verticalGradient(listOf(Color.Transparent, animateDominantColor)))
    ) {

        val constraintSet = ConstraintSet {
            val imageCard = createRefFor("imageCard")
            val playerCard = createRefFor("playerCard")
            val pagerConstraint = createRefFor("pagerConstraint")

            constrain(imageCard) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
            constrain(playerCard) {
                top.linkTo(imageCard.top)
                bottom.linkTo(imageCard.bottom)
                end.linkTo(parent.end)
            }
            constrain(pagerConstraint) {
                top.linkTo(imageCard.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        }
        ConstraintLayout(constraintSet = constraintSet, modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = BottomBarPadding)
            .clickable {
                navController.navigate("music_item_screen")
            }) {

            HorizontalPager(
                state = pagerState,
                modifier = pagerModifier.layoutId("pagerConstraint")
            ) { page ->
                PagerSongItem(song = songItems[page])
            }
            LaunchedEffect(key1 = curSongMetadata) {
                val index = songItems.indexOf(curSongMetadata?.toSong())
                newPageIndex = index
                if (index in 0..songItems.size)
                    pagerState.animateScrollToPage(
                        index, initialVelocity = 0.5f,
                        animationSpec = tween(
                            durationMillis = 700,
                            easing = FastOutSlowInEasing
                        )
                    )
            }


            Card(
                modifier = Modifier
                    .width(SongImageSize)
                    .height(SongImageSize)
                    .layoutId("imageCard"),
                shape = RoundedCornerShape(SongImageRoundness),
                elevation = 8.dp
            ) {
                GlideImage(
                    modifier = Modifier
                        .width(SongImageSize)
                        .height(SongImageSize),
                    imageModel = curSongItem.song?.imageUrl ?: "",
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
                                dominantColor = it.copy(alpha = 0.1f)
                            }
                            CircularRevealedImage(
                                bitmap = imageBitmap,
                                contentDescription = "Music Image",
                                circularRevealedEnabled = false,
                                contentScale = ContentScale.Crop
                            )
                        }
                    },
                    alignment = Alignment.Center
                )
            }
            Card(
                modifier = Modifier
                    .width(SongImageSize)
                    .height(SongImageSize)
                    .layoutId("playerCard"),
                shape = RoundedCornerShape(BottomBarPlayerRoundness),
                elevation = 8.dp,
                onClick = {
                    curSongItem.song?.let { onItemClickListener.onItemClickPlay(it) }
                }
            ) {
                val painter = when (curSongItem.songState) {
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
                Image(
                    painter = painter,
                    contentDescription = stringResource(
                        id = R.string.cd_pause_button
                    ),
                    modifier = Modifier.padding(MusicItemScreenControllerBtnPadding)
                )
            }
        }
    }
}

@Composable
fun PagerSongItem(song: Song?, gradientColor: Color? = null) {
    val constraintSet = ConstraintSet() {
        val startSpacer = createRefFor("startSpacer")
        val textBox = createRefFor("textBox")
        val endSpacer = createRefFor("endSpacer")
        constrain(startSpacer) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
        }
        constrain(textBox) {
            top.linkTo(parent.top)
            start.linkTo(startSpacer.end)
            end.linkTo(endSpacer.start)
            width = Dimension.fillToConstraints
        }
        constrain(endSpacer) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
        }
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = BottomBarPadding),
        constraintSet = constraintSet
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(BottomBarItemSpacer)
                .layoutId("startSpacer")
        )
        Box(modifier = Modifier.layoutId("textBox")) {
            Column {
                Text(
                    text = song?.title ?: "",
                    fontSize = SongItemTextTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song?.subtitle ?: "",
                    fontSize = SongItemTextSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(BottomBarItemSpacer)
                .layoutId("endSpacer")
        )
    }
}