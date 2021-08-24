package com.project.songovermusicapp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.songovermusicapp.R

@Composable
fun MainAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent
) {

    TopAppBar(
        title = {
            Row(Modifier.background(Color.Transparent)) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null
                )
                Icon(
                    painter = painterResource(R.drawable.ic_text_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .heightIn(max = 24.dp)
                )
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.cd_info)
                    )
                }
            }
        },
        elevation = 0.dp,
        backgroundColor = backgroundColor
    )
}