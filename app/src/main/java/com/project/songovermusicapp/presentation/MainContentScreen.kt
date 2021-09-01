package com.project.songovermusicapp.presentation

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.project.songovermusicapp.R
import com.project.songovermusicapp.data.entities.Song
import com.project.songovermusicapp.data.other.Resource
import com.project.songovermusicapp.data.other.Status
import com.project.songovermusicapp.exoplayer.extensions.toSong
import com.project.songovermusicapp.exoplayer.toSong
import com.project.songovermusicapp.presentation.musiclist.MusicList
import com.project.songovermusicapp.presentation.musiclist.OnItemClickListener
import com.project.songovermusicapp.presentation.ui.theme.TabCategoryCollapsedSize
import com.project.songovermusicapp.presentation.ui.theme.TabCategoryExpandedSize
import com.project.songovermusicapp.presentation.ui.viewmodel.MainCategory
import com.project.songovermusicapp.presentation.ui.viewmodel.SongItem
import com.project.songovermusicapp.presentation.util.OnDominantColorListener

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    selectedMainCategory: MainCategory,
    mainCategories: List<MainCategory>,
    onCategorySelected: (MainCategory) -> Unit,
    mediaListClickListener: OnItemClickListener,
    dominantColorListener: OnDominantColorListener,
    dominantColor: Color,
    navController: NavController,
    curPlayingSong: MediaMetadataCompat?,
    playbackState: PlaybackStateCompat?,
    resourceMediaItems: Resource<List<Song>>?,
    curSongQueue: MutableList<MediaSessionCompat.QueueItem>?,
    isShuffle: Boolean,
    isRepeat: Boolean
) {


    var curPlayingSongItem by remember { mutableStateOf(SongItem.stopped()) }

    var songItems by remember { mutableStateOf(emptyList<Song>()) }

    var queueItems by remember { mutableStateOf(emptyList<Song>()) }

    queueItems = curSongQueue?.let{
        it.mapNotNull{item -> item.toSong()}
    }?: emptyList()

    when (resourceMediaItems?.status) {
        Status.SUCCESS -> {
            resourceMediaItems.data?.let {
                songItems = it
            }
        }
        Status.LOADING -> {

        }
        Status.ERROR -> {


        }
    }


    curPlayingSong?.toSong()?.let {
        when (playbackState?.state) {
            PlaybackStateCompat.STATE_PAUSED -> {
                curPlayingSongItem = SongItem.paused(it)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                curPlayingSongItem = SongItem.playing(it)
            }
            else -> {
            }
        }
    }
    val constraints = ConstraintSet {
        val topAppBar = createRefFor("topAppBar")
        val bottomMusicBar = createRefFor("bottomMusicBar")
        val mainContent = createRefFor("mainContent")

        constrain(topAppBar) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(mainContent) {
            top.linkTo(topAppBar.bottom)
            bottom.linkTo(bottomMusicBar.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.fillToConstraints
        }
        constrain(bottomMusicBar) {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    }
    ConstraintLayout(constraintSet = constraints, modifier = Modifier.fillMaxSize()) {

        //val surfaceColor = MaterialTheme.colors.surface
        // From google jetpack compose example
//        val dominantColorState = rememberDominantColorState { color ->
//            // We want a color which has sufficient contrast against the surface color
//            color.contrastAgainst(surfaceColor) >= MinContrastOfPrimaryVsSurface
//        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("topAppBar")
                .background(
                    Brush.verticalGradient(
                        listOf(
                            dominantColor,
                            dominantColor.copy(0.25f)
                        )
                    )
                )
        ) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .statusBarsHeight()
            )
            MainAppBar(
                modifier = Modifier.fillMaxWidth()
            )


        }
        Column(modifier = Modifier.layoutId("mainContent")) {
            if (mainCategories.isNotEmpty()) {
                MainCategoriesTabs(
                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                dominantColor.copy(
                                    0.25f
                                ), Color.Transparent
                            )
                        )
                    ),
                    categories = mainCategories,
                    selectedCategory = selectedMainCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            when (selectedMainCategory) {
                MainCategory.Remote -> {
                    MusicList(
                        list = songItems,
                        itemClickListener = mediaListClickListener,
                        curPlayingSongItem = curPlayingSongItem
                    )
                }
                MainCategory.Local -> {
                    MusicList(
                        list = songItems,
                        itemClickListener = mediaListClickListener,
                        curPlayingSongItem = curPlayingSongItem
                    )
                }
            }
        }
        Column(modifier = Modifier.layoutId("bottomMusicBar")) {
            BottomBar(
                curSongItem = curPlayingSongItem,
                songItems = if(isShuffle) queueItems else songItems,
                modifier = Modifier,
                onItemClickListener = mediaListClickListener,
                dominantColorListener = dominantColorListener,
                navController = navController,
                isShuffle = isShuffle
            )
        }

    }

}

@Composable
fun MainCategoriesTabs(
    modifier: Modifier = Modifier,
    categories: List<MainCategory>,
    selectedCategory: MainCategory,
    onCategorySelected: (MainCategory) -> Unit
) {
    val selectedIndex = categories.indexOfFirst { it == selectedCategory }
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        MainCategoryTabIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
        )
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = indicator,
        modifier = Modifier.background(MaterialTheme.colors.surface.copy(alpha = 0.87f))
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                modifier = modifier,
                selected = index == selectedIndex,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = when (category) {
                            MainCategory.Remote -> stringResource(id = R.string.tab_remote)
                            MainCategory.Local -> stringResource(id = R.string.tab_local)
                        },
                        fontSize = if (index == selectedIndex) TabCategoryExpandedSize else TabCategoryCollapsedSize
                    )
                }
            )
        }
    }
}

@Composable
fun MainCategoryTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface
) {
    Spacer(
        modifier = modifier.background(
            color,
            RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)
        )
    )

}