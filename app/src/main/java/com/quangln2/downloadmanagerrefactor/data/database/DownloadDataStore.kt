package com.quangln2.downloadmanagerrefactor.data.database

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quangln2.downloadmanagerrefactor.DownloadManagerApplication.Companion.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DownloadDataStore {
    private val DOWNLOAD_LIST = stringPreferencesKey("download_list")
    val getDownloadList: (Context) -> Flow<String> = {
        it.dataStore.data.map { preferences ->
            preferences[DOWNLOAD_LIST] ?: ""
        }
    }
    suspend fun setDownloadList(context: Context, downloadList: String) {
        context.dataStore.edit { preferences ->
            preferences[DOWNLOAD_LIST] = downloadList
        }
    }
}