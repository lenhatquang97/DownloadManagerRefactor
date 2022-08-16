package com.quangln2.downloadmanagerrefactor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.database.DownloadDatabase

class DownloadManagerApplication : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: DownloadManagerApplication? = null
        private fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        val database by lazy { DownloadDatabase.getDatabase(applicationContext()) }
        val downloadRepository by lazy { ServiceLocator.provideDownloadRepository(database.downloadDao()) }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ConstantClass.CHANNEL_NAME
            val descriptionText = ConstantClass.CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(ConstantClass.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager? =
                ContextCompat.getSystemService(context, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

