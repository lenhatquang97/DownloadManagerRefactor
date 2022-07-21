package com.quangln2.mydownloadmanager

import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.source.local.LocalDataSourceImpl
import com.quangln2.mydownloadmanager.data.source.remote.RemoteDataSourceImpl

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

    fun initializeStructureDownFile(): StructureDownFile {
        return StructureDownFile(
            "",
            ConstantClass.FILE_NAME_DEFAULT,
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