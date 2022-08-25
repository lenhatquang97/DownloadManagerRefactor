package com.quangln2.downloadmanagerrefactor.data.source.remote

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow


class RemoteDataSourceImpl : RemoteDataSource {

    override fun addNewDownloadInfo(
        url: String,
        downloadTo: String,
        file: StructureDownFile
    ) {
        file.protocolInterface.addNewDownloadInfo(url, downloadTo, file)
    }

    override fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile =
        file.protocolInterface.fetchDownloadInfo(file)

    override fun downloadAFile(
        file: StructureDownFile,
        context: Context
    ): Flow<StructureDownFile> = file.protocolInterface.downloadAFile(file, context)


    override fun resumeDownload(file: StructureDownFile, context: Context) =
        file.protocolInterface.resumeDownload(file, context)

    override fun pauseDownload(file: StructureDownFile) =
        file.protocolInterface.pauseDownload(file)

    override fun stopDownload(file: StructureDownFile, context: Context) =
        file.protocolInterface.stopDownload(file, context)

    override fun retryDownload(file: StructureDownFile, context: Context) =
        file.protocolInterface.retryDownload(file, context)


}