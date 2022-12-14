package com.quangln2.downloadmanagerrefactor.data.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.source.local.LocalDataSource
import com.quangln2.downloadmanagerrefactor.data.source.remote.RemoteDataSource
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import kotlinx.coroutines.flow.Flow


class DefaultDownloadRepository(
    private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource
) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(StructureDownFile: StructureDownFile) {
        localDataSource.insert(StructureDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(StructureDownFile: StructureDownFile) {
        localDataSource.update(StructureDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteFromList(file: StructureDownFile, context: Context) {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra("item", file)
        intent.putExtra("command", "KillNotification")
        context.startService(intent)
        localDataSource.deleteFromDatabase(file)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePermanently(file: StructureDownFile, context: Context) {
        localDataSource.deletePermanently(file, context)
        localDataSource.deleteFromDatabase(file)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun doesDownloadLinkExist(file: StructureDownFile): Boolean {
        return localDataSource.doesDownloadLinkExist(file)
    }

    fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile) =
        remoteDataSource.addNewDownloadInfo(url, downloadTo, file)

    fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile =
        remoteDataSource.fetchDownloadInfo(file)

    fun writeToFileAPI29Below(file: StructureDownFile) =
        localDataSource.writeToFileAPI29Below(file)

    fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile> =
        remoteDataSource.downloadAFileWithChunks(file, context)

    fun retryDownload(file: StructureDownFile, context: Context) {
        remoteDataSource.retryDownload(file, context)
    }

    fun openDownloadFile(item: StructureDownFile, context: Context) =
        localDataSource.openDownloadFile(item, context)

    fun resumeDownload(file: StructureDownFile, context: Context) = remoteDataSource.resumeDownload(file, context)
    fun pauseDownload(file: StructureDownFile) = remoteDataSource.pauseDownload(file)
    fun stopDownload(file: StructureDownFile, context: Context) = remoteDataSource.stopDownload(file, context)
    fun vibratePhone(context: Context) = localDataSource.vibratePhone(context)

}
