package com.quangln2.mydownloadmanager.data.repository

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.io.BufferedInputStream
import java.io.OutputStream
import java.net.URLConnection

interface DownloadRepository {
    fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile)
    fun fetchDownloadInfo(file: StrucDownFile): Job
    fun writeToFileAPI29Above(file: StrucDownFile, context: Context): Job
    fun writeToFileAPI29Below(file: StrucDownFile): String
    fun downloadAFile(file: StrucDownFile, context: Context): Job

    fun resumeDownload(file: StrucDownFile)
    fun pauseDownload(file: StrucDownFile)
    fun stopDownload(file: StrucDownFile)
    suspend fun retryDowwnload(file: StrucDownFile, context: Context)
    fun queueDownload(file: StrucDownFile)

    suspend fun copyFile()
    suspend fun openDownloadFile()
}