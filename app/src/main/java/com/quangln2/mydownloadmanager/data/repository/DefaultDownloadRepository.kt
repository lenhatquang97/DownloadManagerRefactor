package com.quangln2.mydownloadmanager.data.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.source.local.LocalDataSource
import com.quangln2.mydownloadmanager.data.source.remote.RemoteDataSource
import com.quangln2.mydownloadmanager.service.DownloadService
import kotlinx.coroutines.flow.Flow


class DefaultDownloadRepository(
    private val downloadDao: DownloadDao,
    private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource
) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(StructureDownFile: StructureDownFile) {
        downloadDao.insert(StructureDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(StructureDownFile: StructureDownFile) {
        downloadDao.update(StructureDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteFromList(StructureDownFile: StructureDownFile, context: Context) {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra("item", StructureDownFile)
        intent.putExtra("command", "KillNotification")
        context.startService(intent)
        downloadDao.delete(StructureDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePermanently(file: StructureDownFile, context: Context) {
        localDataSource.deletePermanently(file, context)
        downloadDao.delete(file)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun doesDownloadLinkExist(file: StructureDownFile): Boolean {
        return downloadDao.doesDownloadLinkExist(file.downloadLink) == 1
    }

    fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile) =
        remoteDataSource.addNewDownloadInfo(url, downloadTo, file)

    fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile =
        remoteDataSource.fetchDownloadInfo(file)

    fun writeToFileAPI29Above(file: StructureDownFile, context: Context) =
        localDataSource.writeToFileAPI29Above(file, context)

    fun writeToFileAPI29Below(file: StructureDownFile) =
        localDataSource.writeToFileAPI29Below(file)

    fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile> =
        remoteDataSource.downloadAFile(file, context)

    fun retryDownload(file: StructureDownFile, context: Context) {
        remoteDataSource.retryDownload(file, context)
    }


    fun openDownloadFile(item: StructureDownFile, context: Context) =
        localDataSource.openDownloadFile(item, context)


}
