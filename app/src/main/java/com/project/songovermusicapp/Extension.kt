package com.project.songovermusicapp

import android.media.browse.MediaBrowser
import android.support.v4.media.MediaBrowserCompat
import com.project.songovermusicapp.presentation.ui.viewmodel.MainCategory

fun MediaBrowserCompat.reconnect(){
    if(this.isConnected)
        disconnect()
    connect()
}
