package com.quangln2.mydownloadmanager

import android.app.Application
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class DownloadManagerApplication: Application() {
    val database by lazy{DownloadDatabase.getDatabase(this)}
    val downloadRepository: DownloadRepository get() = ServiceLocator.provideDownloadRepository(database.downloadDao())
}