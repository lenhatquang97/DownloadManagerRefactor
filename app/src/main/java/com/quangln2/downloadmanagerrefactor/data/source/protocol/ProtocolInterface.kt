package com.quangln2.downloadmanagerrefactor.data.source.protocol

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

//HTTP, Socket
interface ProtocolInterface : Serializable {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile)
    fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile
    fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile>
    fun resumeDownload(file: StructureDownFile, context: Context)
    fun pauseDownload(file: StructureDownFile)
    fun stopDownload(file: StructureDownFile, context: Context)
    fun retryDownload(file: StructureDownFile, context: Context)

}