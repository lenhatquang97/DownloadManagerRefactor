package com.quangln2.mydownloadmanager.data.repository

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile)
    fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile
    fun writeToFileAPI29Above(file: StrucDownFile, context: Context)
    fun writeToFileAPI29Below(file: StrucDownFile)
    fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile>

    fun resumeDownload(file: StrucDownFile)
    fun pauseDownload(file: StrucDownFile)
    fun stopDownload(file: StrucDownFile)
    fun retryDownload(file: StrucDownFile, context: Context)
    fun queueDownload(file: StrucDownFile)

    suspend fun copyFile()
    fun openDownloadFile(item: StrucDownFile, context: Context)

    suspend fun insert(strucDownFile: StrucDownFile)
    suspend fun update(strucDownFile: StrucDownFile)

    fun getBytesFromExistingFile(file: StrucDownFile, context: Context): Long
}