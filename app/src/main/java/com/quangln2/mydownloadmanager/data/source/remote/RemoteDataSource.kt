package com.quangln2.mydownloadmanager.data.source.remote

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile)
    fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile
    fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile>
    fun resumeDownload(file: StructureDownFile)
    fun pauseDownload(file: StructureDownFile)
    fun stopDownload(file: StructureDownFile)
    fun retryDownload(file: StructureDownFile, context: Context)
    fun queueDownload(file: StructureDownFile)
}