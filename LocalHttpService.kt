package com.helper.watchbridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD

class LocalHttpService : Service() {
    private var server: NanoServer? = null
    private val gson = Gson()

    companion object {
        private const val CHANNEL_ID = "projectivy_helper_channel"
        private const val NOTIF_ID = 12345
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, createNotification())
        server = NanoServer(9191, this)
        server?.start()
    }

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Projectivy Helper Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(chan)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Projectivy Helper Running")
            .setContentText("Providing Watch Next & Now Playing data")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    inner class NanoServer(port: Int, val ctx: Context) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession?): Response {
            val remoteIp = session?.remoteIpAddress
            if (remoteIp != "127.0.0.1" && remoteIp != "::1") {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")
            }

            val now = NowPlayingListener.current
            val scanner = UsageScanner(ctx.applicationContext)
            val watch = scanner.getRecentApps(8)
            val payload = BridgePayload(nowPlaying = now, watchNext = watch)
            val json = gson.toJson(payload)
            return newFixedLengthResponse(Response.Status.OK, "application/json", json)
        }
    }
}
