package com.quangln2.mydownloadmanager

import android.app.Application
import android.app.DownloadManager
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class DownloadManagerApplication: Application() {
    val downloadRepository: DownloadRepository get() = ServiceLocator.provideDownloadRepository()
    override fun onCreate() {
        super.onCreate()
    }
}