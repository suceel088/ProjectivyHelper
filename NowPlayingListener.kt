package com.helper.watchbridge

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NowPlayingListener : NotificationListenerService() {
    companion object {
        @Volatile
        var current: MediaItem? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notif = sbn.notification
        val extras = notif.extras

        val category = notif.category
        if (category != Notification.CATEGORY_TRANSPORT && category != Notification.CATEGORY_MEDIA) {
            return
        }

        val title = extras.getString(Notification.EXTRA_TITLE) ?: sbn.packageName
        val subtitle = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val playbackState = extras.getInt("android.media.session.playbackstate.state", -1)

        current = MediaItem(
            id = sbn.key ?: sbn.id.toString(),
            title = title,
            subtitle = subtitle,
            appPackage = sbn.packageName,
            timestamp = System.currentTimeMillis(),
            playbackState = playbackState
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        current = null
    }
}
