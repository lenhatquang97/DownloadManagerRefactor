package com.quangln2.mydownloadmanager.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.quangln2.mydownloadmanager.MainActivity
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile


class DownloadService(): Service() {
    private val CHANNEL_ID = "download_notification"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
            .setContentTitle("DownloadManager")
            .setContentText("Welcome to DownloadManager")
            .setGroup(CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        startForeground(1, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val fileName = intent.getStringExtra("fileName")
            val content = intent.getStringExtra("content")
            val id = intent.getIntExtra("id", 1)
            val progress = intent.getIntExtra("progress", 0)

            val resultIntent = Intent(this, MainActivity::class.java)
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
                .setContentTitle(fileName)
                .setContentText(content)
                .setGroup(CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(progress == -1){
                manager.notify(id, builder.build())
            } else if(progress == 100){
                builder.setContentIntent(resultPendingIntent)
                manager.notify(id, builder.build())
            }
            else {
                builder.setProgress(100, progress, false)
                manager.notify(id, builder.build())
            }
        }
        return START_NOT_STICKY
    }
}