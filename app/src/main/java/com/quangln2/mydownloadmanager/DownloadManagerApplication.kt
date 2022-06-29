package com.quangln2.mydownloadmanager

import android.app.Application
import android.content.Context
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadManagerApplication(): Application() {
    val database by lazy{ DownloadDatabase.getDatabase(this)}
    val downloadRepository by lazy{ServiceLocator.provideDownloadRepository(database.downloadDao())}
}