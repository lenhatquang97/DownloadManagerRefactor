package com.quangln2.mydownloadmanager.data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.source.local.LocalDataSource
import com.quangln2.mydownloadmanager.data.source.remote.RemoteDataSource
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class DefaultDownloadRepository(
    private val downloadDao: DownloadDao,
    private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource
) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(strucDownFile: StrucDownFile) {
        downloadDao.insert(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(strucDownFile: StrucDownFile) {
        downloadDao.update(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteFromList(strucDownFile: StrucDownFile) {
        downloadDao.delete(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePermanently(file: StrucDownFile, context: Context) {
        localDataSource.deletePermanently(file, context)
        downloadDao.delete(file)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun doesDownloadLinkExist(file: StrucDownFile): Boolean {
        return downloadDao.doesDownloadLinkExist(file.downloadLink) == 1
    }

    fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) =
        remoteDataSource.addNewDownloadInfo(url, downloadTo, file)

    fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile =
        remoteDataSource.fetchDownloadInfo(file)

    fun writeToFileAPI29Above(file: StrucDownFile, context: Context) =
        localDataSource.writeToFileAPI29Above(file, context)

    fun writeToFileAPI29Below(file: StrucDownFile) =
        localDataSource.writeToFileAPI29Below(file)

    fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile> =
        remoteDataSource.downloadAFile(file, context)

    fun resumeDownload(file: StrucDownFile) = remoteDataSource.resumeDownload(file)
    fun pauseDownload(file: StrucDownFile) = remoteDataSource.pauseDownload(file)
    fun stopDownload(file: StrucDownFile) = remoteDataSource.stopDownload(file)
    fun retryDownload(file: StrucDownFile, context: Context){
        remoteDataSource.retryDownload(file, context)
    }


    fun queueDownload(file: StrucDownFile) = remoteDataSource.queueDownload(file)
    fun openDownloadFile(item: StrucDownFile, context: Context) =
        localDataSource.openDownloadFile(item, context)


}
