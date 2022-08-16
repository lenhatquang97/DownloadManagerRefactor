package com.quangln2.downloadmanagerrefactor.data.source.remote

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile)
    fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile
    fun downloadAFileWithChunks(file: StructureDownFile, context: Context): Flow<StructureDownFile>
    fun pauseDownload(file: StructureDownFile)
    fun stopDownload(file: StructureDownFile)
    fun retryDownload(file: StructureDownFile, context: Context)
    fun resumeDownload(file: StructureDownFile, context: Context)
}