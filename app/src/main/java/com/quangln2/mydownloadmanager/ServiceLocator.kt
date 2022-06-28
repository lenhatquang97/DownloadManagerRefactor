package com.quangln2.mydownloadmanager

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

object ServiceLocator {
    var downloadRepository: DownloadRepository? = null
    fun provideDownloadRepository(): DownloadRepository {
        synchronized(this){
            return downloadRepository ?: downloadRepository ?: createDownloadRepository()
        }
    }
    private fun createDownloadRepository(): DownloadRepository {
        return DefaultDownloadRepository()
    }
    fun initializeStrucDownFile(): StrucDownFile {
        return StrucDownFile(
            -1,
            "test",
            "test",
            "Documents",
            -1,
            0,
            DownloadStatusState.COMPLETED,
            "*/*",
            "test.apk",
            null
        )
    }
}