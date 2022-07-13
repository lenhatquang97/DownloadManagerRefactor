package com.quangln2.mydownloadmanager

import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.datasource.LocalDataSourceImpl
import com.quangln2.mydownloadmanager.data.datasource.RemoteDataSourceImpl
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

object ServiceLocator {
    var downloadRepository: DownloadRepository? = null
    fun provideDownloadRepository(database: DownloadDao): DownloadRepository {
        synchronized(this){
            return downloadRepository ?: downloadRepository ?: createDownloadRepository(database)
        }
    }
    private fun createDownloadRepository(database: DownloadDao): DownloadRepository {
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