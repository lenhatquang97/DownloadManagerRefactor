package com.quangln2.mydownloadmanager

import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.datasource.LocalDataSourceImpl
import com.quangln2.mydownloadmanager.data.datasource.RemoteDataSourceImpl
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

object ServiceLocator {
    var downloadRepository: DefaultDownloadRepository? = null
    fun provideDownloadRepository(database: DownloadDao): DefaultDownloadRepository {
        synchronized(this) {
            return downloadRepository ?: downloadRepository ?: createDownloadRepository(database)
        }
    }

    private fun createDownloadRepository(database: DownloadDao): DefaultDownloadRepository {
        return DefaultDownloadRepository(database, LocalDataSourceImpl(), RemoteDataSourceImpl())
    }

    fun initializeStrucDownFile(): StrucDownFile {
        return StrucDownFile(
            "",
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