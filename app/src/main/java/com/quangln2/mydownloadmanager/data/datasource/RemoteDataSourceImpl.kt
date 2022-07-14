package com.quangln2.mydownloadmanager.data.datasource

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RemoteDataSourceImpl : RemoteDataSource {

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) {
        file.id = UUID.randomUUID().toString()
        file.downloadLink = url
        file.downloadTo = downloadTo
        file.bytesCopied = 0
    }

    private fun getMimeType(url: String?): String {
        var type = "*/*"
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }

    override fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile {
        val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.doOutput = true
        connection.requestMethod = "GET"
        try {
            val responseCode = connection.responseCode
            return if (responseCode == 200) {
                file.fileName =
                    URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
                file.mimeType =
                    if (connection.contentType == null) getMimeType(file.downloadLink) else connection.contentType
                file.size = connection.contentLength.toLong()
                file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
                connection.disconnect()
                file
            } else {
                val initFile = ServiceLocator.initializeStrucDownFile()
                file.fileName = initFile.fileName
                file.size = initFile.size
                initFile
            }
        } catch (e: Exception) {
            val initFile = ServiceLocator.initializeStrucDownFile()
            file.fileName = initFile.fileName
            file.size = initFile.size
            return initFile
        }

    }

    override fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile> = flow {
        try {
            val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
            connection.setRequestProperty("Range", "bytes=${file.bytesCopied}-")
            connection.doInput = true
            connection.doOutput = true
            val inp = BufferedInputStream(connection.inputStream)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (file.uri == null) {
                    return@flow
                }
                val out = context.contentResolver.openOutputStream(Uri.parse(file.uri), "wa")
                if (out != null) {
                    val data = ByteArray(1024)
                    var x = inp.read(data, 0, 1024)
                    while (x >= 0) {
                        out.write(data, 0, x)
                        file.bytesCopied += x
                        emit(file)
                        if (file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED) {
                            out.close()
                            connection.disconnect()
                            break
                        }
                        x = inp.read(data, 0, 1024)
                    }
                    out.close()
                    connection.disconnect()
                }

            } else {
                val fos =
                    if (file.bytesCopied == 0L) FileOutputStream(file.downloadTo) else FileOutputStream(
                        file.downloadTo,
                        true
                    )
                val data = ByteArray(1024)
                var x = inp.read(data, 0, 1024)
                while (x >= 0) {
                    fos.write(data, 0, x)
                    file.bytesCopied += x
                    emit(file)
                    if (file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED) {
                        fos.close()
                        connection.disconnect()
                        break
                    }
                    x = inp.read(data, 0, 1024)
                }
                fos.close()
                connection.disconnect()
            }
        } catch (e: Exception) {
            emit(file.copy(downloadState = DownloadStatusState.FAILED))
        }

    }.debounce(50).flowOn(Dispatchers.IO)


    override fun resumeDownload(file: StrucDownFile) {
        file.downloadState = DownloadStatusState.DOWNLOADING
    }

    override fun pauseDownload(file: StrucDownFile) {
        file.downloadState = DownloadStatusState.PAUSED
    }

    override fun stopDownload(file: StrucDownFile) {
        file.bytesCopied = 0
        file.downloadState = DownloadStatusState.FAILED
    }

    override fun retryDownload(file: StrucDownFile, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            if (file.uri != null) {
                resolver.delete(Uri.parse(file.uri), null, null)
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.downloadLink))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            file.uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues).toString()
        } else {
            val filePath = File(file.downloadTo)
            if (filePath.exists()) {
                filePath.delete()
            }
            file.downloadTo = ""
            file.uri = null
        }
        file.bytesCopied = 0
        file.downloadState = DownloadStatusState.DOWNLOADING
    }

    override fun queueDownload(file: StrucDownFile) {
        file.downloadState = DownloadStatusState.QUEUED
    }

}