package com.quangln2.mydownloadmanager.data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.datasource.LocalDataSource
import com.quangln2.mydownloadmanager.data.datasource.RemoteDataSource
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.flow.Flow


class DefaultDownloadRepository(
    private val downloadDao: DownloadDao,
    private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource
) : DownloadRepository {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(strucDownFile: StrucDownFile) {
        downloadDao.insert(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(strucDownFile: StrucDownFile) {
        downloadDao.update(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun deleteFromList(strucDownFile: StrucDownFile) {
        downloadDao.delete(strucDownFile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun deletePermanently(file: StrucDownFile, context: Context) {
        localDataSource.deletePermanently(file, context)
        downloadDao.delete(file)
    }

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) =
        remoteDataSource.addNewDownloadInfo(url, downloadTo, file)

    override fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile =
        remoteDataSource.fetchDownloadInfo(file)

    override fun writeToFileAPI29Above(file: StrucDownFile, context: Context) =
        localDataSource.writeToFileAPI29Above(file, context)

    override fun writeToFileAPI29Below(file: StrucDownFile) =
        localDataSource.writeToFileAPI29Below(file)

    override fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile> =
        remoteDataSource.downloadAFile(file, context)

    override fun resumeDownload(file: StrucDownFile) = remoteDataSource.resumeDownload(file)
    override fun pauseDownload(file: StrucDownFile) = remoteDataSource.pauseDownload(file)
    override fun stopDownload(file: StrucDownFile) = remoteDataSource.stopDownload(file)
    override fun retryDownload(file: StrucDownFile, context: Context) =
        remoteDataSource.retryDownload(file, context)

    override fun queueDownload(file: StrucDownFile) = remoteDataSource.queueDownload(file)
    override suspend fun copyFile() = localDataSource.copyFile()
    override fun openDownloadFile(item: StrucDownFile, context: Context) =
        localDataSource.openDownloadFile(item, context)


}
