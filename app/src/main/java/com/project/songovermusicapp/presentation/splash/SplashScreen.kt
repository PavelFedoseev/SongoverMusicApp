package com.project.songovermusicapp.presentation.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.project.songovermusicapp.R
import com.project.songovermusicapp.presentation.ui.theme.SongoverMusicAppTheme
import com.project.songovermusicapp.presentation.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, mainViewModel: MainViewModel) {

    val scale = remember {
        Animatable(0f)
    }

        LaunchedEffect(key1 = true) {
            scale.animateTo(2.5f, animationSpec = tween(
                durationMillis = 1000,
                easing = {
                    OvershootInterpolator(2.5f).getInterpolation(it)
                }
            ))
            delay(500)
            navController.navigate("main_screen")
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_text_logo),
                contentDescription = stringResource(
                    id = R.string.cd_splash
                ),
                modifier = Modifier.scale(scale.value)
            )
        }
}