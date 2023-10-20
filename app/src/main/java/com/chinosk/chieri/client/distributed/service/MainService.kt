package com.chinosk.chieri.client.distributed.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chinosk.chieri.client.distributed.R

class MainService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == EXIT_ACTION) {
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chieri Bot",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val exitIntent = Intent(this, this::class.java).apply {
            action = EXIT_ACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingExitIntent = PendingIntent.getService(this, 0, exitIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.chieri_bot_client))
            .setContentText(getString(R.string.noticeContent))
            .setSmallIcon(R.mipmap.ic_launcher)
            // .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.play_store_512))
            .addAction(R.mipmap.ic_launcher, getString(R.string.noticeClose), pendingExitIntent)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }

    companion object {
        private const val CHANNEL_ID = "chieri_notice"
        const val EXIT_ACTION = "com.chinosk.chieri.client.distributed.EXIT"
    }
}