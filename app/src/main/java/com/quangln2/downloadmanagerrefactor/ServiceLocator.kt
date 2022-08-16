package com.quangln2.downloadmanagerrefactor

import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.database.DownloadDao
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import com.quangln2.downloadmanagerrefactor.data.source.local.LocalDataSourceImpl
import com.quangln2.downloadmanagerrefactor.data.source.remote.RemoteDataSourceImpl

object ServiceLocator {
    var downloadRepository: DefaultDownloadRepository? = null
    fun provideDownloadRepository(database: DownloadDao): DefaultDownloadRepository {
        synchronized(this) {
            return downloadRepository ?: downloadRepository ?: createDownloadRepository(database)
        }
    }

    private fun createDownloadRepository(database: DownloadDao): DefaultDownloadRepository {
        return DefaultDownloadRepository(LocalDataSourceImpl(database), RemoteDataSourceImpl())
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
            null,
            mutableListOf(0L, 0L, 0L, 0L, 0L),
            null
        )
    }
}