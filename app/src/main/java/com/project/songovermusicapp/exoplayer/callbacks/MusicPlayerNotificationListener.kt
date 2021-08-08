package com.project.songovermusicapp.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.project.songovermusicapp.data.constants.Constants.NOTIFICATION_ID
import com.project.songovermusicapp.exoplayer.MusicService

class MusicPlayerNotificationListener(
    private val musicService : MusicService
) : PlayerNotificationManager.NotificationListener {

    //свайп нотификации для отмены
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    //нотификация размешена
    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if(ongoing && !isForegroundService){
                ContextCompat.startForegroundService(this,
                Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}