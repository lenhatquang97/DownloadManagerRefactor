package com.quangln2.mydownloadmanager.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile


class DownloadService(): Service() {
    private val CHANNEL_ID = "download_notification"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val fileName = intent.getStringExtra("fileName")
            val content = intent.getStringExtra("content")
            val id = intent.getIntExtra("id", 1)
            val progress = intent.getIntExtra("progress", 0)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
                //file.filename
                .setContentTitle(fileName)
                //file.content
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            if(progress == -1){
                manager.notify(id, builder.build())
                startForeground(id, builder.build())
            } else {
                builder.setProgress(100, progress, false)
                manager.notify(id, builder.build())
                startForeground(id, builder.build())
            }
        }
        return START_NOT_STICKY
    }
}