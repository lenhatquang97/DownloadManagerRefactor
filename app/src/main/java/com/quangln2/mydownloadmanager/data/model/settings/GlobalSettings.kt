package com.quangln2.mydownloadmanager.data.model.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.quangln2.mydownloadmanager.DownloadManagerApplication.Companion.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object GlobalSettings {
    private val IS_VIBRATED = booleanPreferencesKey("isVibrated")
    val getVibrated: (Context) -> Flow<Boolean> = { it ->
        it.dataStore.data.map {
            it[IS_VIBRATED] ?: false
        }
    }

    suspend fun setVibrated(context: Context, isVibrated: Boolean) {
        context.dataStore.edit {
            it[IS_VIBRATED] = isVibrated
        }
    }


    var numsOfMaxDownloadThreadExported = 2
    private val MAXIMUM_DOWNLOAD_THREAD = floatPreferencesKey("maximumDownloadThread")
    val getMaximumDownloadThread: (Context) -> Flow<Float> = { it ->
        it.dataStore.data.map {
            it[MAXIMUM_DOWNLOAD_THREAD] ?: 2.0f
        }
    }

    suspend fun setMaximumDownloadThread(context: Context, maximumDownloadThread: Float) {
        context.dataStore.edit {
            it[MAXIMUM_DOWNLOAD_THREAD] = maximumDownloadThread
        }
    }



}