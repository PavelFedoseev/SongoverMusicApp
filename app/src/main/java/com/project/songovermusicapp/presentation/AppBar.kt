package com.project.songovermusicapp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.songovermusicapp.APPBAR_TEXT
import com.project.songovermusicapp.R
import com.project.songovermusicapp.presentation.ui.theme.AppBarTextLogoSize
import com.project.songovermusicapp.presentation.ui.theme.TopAppBarHeight
import com.project.songovermusicapp.presentation.ui.theme.TopAppBarLogosPadding

@Composable
fun MainAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent
) {

    Row(
        modifier = modifier
            .height(TopAppBarHeight)
            .background(backgroundColor), verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .background(Color.Transparent)
                //.padding(TopAppBarLogosPadding)
        ) {
            Image(
                modifier = Modifier.size(AppBarTextLogoSize),
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = null
            )
            //Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.height(AppBarTextLogoSize),
                text = APPBAR_TEXT,
                fontSize = 30.sp
            )
        }
//        actions = {
//            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
//                IconButton(onClick = { }) {
//                    Icon(
//                        imageVector = Icons.Default.Info,
//                        contentDescription = stringResource(R.string.cd_info)
//                    )
//                }
//            }
//        },
//        elevation = 0.dp,
    }
}