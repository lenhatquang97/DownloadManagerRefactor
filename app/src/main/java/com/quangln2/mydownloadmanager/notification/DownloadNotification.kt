package com.quangln2.mydownloadmanager.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile

class DownloadNotification(context: Context, file: StrucDownFile) {
        val CHANNEL_ID = "download_notification"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
            .setContentTitle(file.fileName)
            .setContentText(file.convertToSizeUnit() + " - " + file.downloadState.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        companion object{
            fun createNotificationChannel(context: Context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "Download Channel"
                    val descriptionText = "This is a notification channel for downloading"
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel("download_notification", name, importance).apply {
                        description = descriptionText
                    }
                    // Register the channel with the system
                    val notificationManager: NotificationManager? = getSystemService(context, NotificationManager::class.java)
                    notificationManager?.createNotificationChannel(channel)
                }
            }
        }
        fun showNotification(context: Context, file: StrucDownFile){

            with(NotificationManagerCompat.from(context)){
                notify(file.fileName.hashCode(), builder.build())
            }
        }
        fun showProgress(context: Context, file: StrucDownFile){
            val progress = ((file.bytesCopied.toFloat() / file.size.toFloat()) * 100.0).toInt()
            builder.setProgress(100, progress, false)
            with(NotificationManagerCompat.from(context)){
                notify(file.fileName.hashCode(), builder.build())
            }
        }
}