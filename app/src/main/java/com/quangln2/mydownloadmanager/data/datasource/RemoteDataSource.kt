package com.quangln2.mydownloadmanager.data.datasource

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile)
    fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile
    fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile>
    fun resumeDownload(file: StrucDownFile)
    fun pauseDownload(file: StrucDownFile)
    fun stopDownload(file: StrucDownFile)
    fun retryDownload(file: StrucDownFile, context: Context)
    fun queueDownload(file: StrucDownFile)
}