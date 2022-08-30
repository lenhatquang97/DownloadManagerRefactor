package com.quangln2.downloadmanagerrefactor.data.source.remote

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.source.protocol.SocketProtocol
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


    override fun resumeDownload(file: StructureDownFile, context: Context){
        if(file.protocol == "Socket"){
            val ip = file.downloadLink.split(":")[0]
            val port = file.downloadLink.split(":")[1].split("/")[0]
            file.protocolInterface = SocketProtocol(ip, port.toInt())
        }
        file.protocolInterface.resumeDownload(file, context)
    }


    override fun pauseDownload(file: StructureDownFile) =
        file.protocolInterface.pauseDownload(file)

    override fun stopDownload(file: StructureDownFile, context: Context) =
        file.protocolInterface.stopDownload(file, context)

    override fun retryDownload(file: StructureDownFile, context: Context) =
        file.protocolInterface.retryDownload(file, context)


}