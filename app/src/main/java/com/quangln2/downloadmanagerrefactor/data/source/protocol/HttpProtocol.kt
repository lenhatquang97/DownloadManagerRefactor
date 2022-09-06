package com.quangln2.downloadmanagerrefactor.data.source.protocol

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.quangln2.downloadmanagerrefactor.ServiceLocator
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.model.FromTo
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class HttpProtocol : ProtocolInterface {
    companion object {
        const val numberOfHTTPChunks = 5
    }

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile) {
        file.id = UUID.randomUUID().toString()
        file.downloadLink = url
        file.downloadTo = downloadTo
        file.bytesCopied = 0
        file.chunkNames = (0 until numberOfHTTPChunks).map { UUID.randomUUID().toString() }.toMutableList()
    }

    private fun getMimeType(url: String?): String {
        var type = "*/*"
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }

    private fun deleteTempFiles(file: StructureDownFile, context: Context) {
        (0 until numberOfHTTPChunks).forEach {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.chunkNames[it])
            val fos = File(appSpecificExternalDir.absolutePath)
            if (fos.exists()) {
                fos.delete()
            }
        }
    }

    override fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile {
        try {
            val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.doInput = true
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            return if (responseCode == 200) {
                file.fileName =
                    URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
                file.mimeType =
                    if (connection.contentType == null) getMimeType(file.downloadLink) else connection.contentType
                file.size = connection.contentLength.toLong()
                file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
                connection.disconnect()
                file.listChunks = (0 until numberOfHTTPChunks).map {
                    val tmp = file.size / numberOfHTTPChunks
                    val endVal =
                        if (it == numberOfHTTPChunks - 1) file.size else tmp * (it + 1) - 1
                    FromTo(tmp * it, endVal, tmp * it)
                }.toMutableList()
                file
            } else {
                val initFile = ServiceLocator.initializeStructureDownFile()
                file.fileName = initFile.fileName
                file.size = initFile.size
                initFile
            }
        } catch (e: Exception) {
            Log.d("FileError", e.toString())
            val initFile = ServiceLocator.initializeStructureDownFile()
            file.fileName = initFile.fileName
            file.size = initFile.size
            return initFile
        }
    }

    override fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                (0 until numberOfHTTPChunks).map {
                    val deferred = async(Dispatchers.IO) {
                        val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
                        connection.setRequestProperty("Connection", "Keep-Alive")
                        connection.doInput = true
                        val from = if (file.listChunks[it].curr == 0L) file.listChunks[it].from
                        else file.listChunks[it].curr
                        val to = file.listChunks[it].to
                        if (from >= to) return@async
                        connection.setRequestProperty("Range", "bytes=${from}-${to}")
                        val inp = BufferedInputStream(connection.inputStream)
                        val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.chunkNames[it])
                        val fos = FileOutputStream(
                            appSpecificExternalDir.absolutePath,
                            true
                        )
                        val bufferSize = 4 * 1024
                        val data = ByteArray(bufferSize)
                        var x = inp.read(data, 0, bufferSize)
                        while (x >= 0) {
                            fos.write(data, 0, x)
                            file.listChunks[it].curr += x.toLong()
                            file.bytesCopied = file.listChunks.map {
                                minOf(it.curr, it.to) - it.from + 1
                            }.reduce { a, b -> a + b } - 1
                            if (file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED) {
                                fos.close()
                                connection.disconnect()
                                return@async
                            }
                            x = inp.read(data, 0, bufferSize)
                            send(file)
                        }
                    }
                    deferred
                }.awaitAll()
            }
        } catch (e: Exception) {
            Log.d("HttpProtocol", e.toString())
            send(file.copy(downloadState = DownloadStatusState.FAILED))
            deleteTempFiles(file, context)
        }
    }.flowOn(Dispatchers.IO)

    override fun resumeDownload(file: StructureDownFile, context: Context) {
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == file.id }
        if (index != null && index != -1) {
            val currentFile = currentList[index]
            (0 until numberOfHTTPChunks).forEach {
                val doesFileExist = DownloadUtil.isFileExistingInFilesDir(currentFile.chunkNames[it], context)
                if (!doesFileExist) {
                    currentFile.downloadState = DownloadStatusState.FAILED
                    DownloadManagerController._progressFile.value = currentFile
                    return
                }
            }

            file.listChunks = currentFile.listChunks.mapIndexed { index, value ->
                value.copy(curr = value.from + DownloadUtil.sizeOfFilesDir(file.chunkNames[index], context))
            }.toMutableList()
            currentFile.downloadState = DownloadStatusState.DOWNLOADING
            DownloadManagerController._progressFile.value = currentFile
        }
    }

    override fun pauseDownload(file: StructureDownFile) {
        file.downloadState = DownloadStatusState.PAUSED
        DownloadManagerController._progressFile.value = file
    }

    override fun stopDownload(file: StructureDownFile, context: Context) {
        file.bytesCopied = 0
        file.listChunks = file.listChunks.map { it.copy(curr = it.from) }.toMutableList()
        file.downloadState = DownloadStatusState.FAILED
        CoroutineScope(Dispatchers.IO).launch {
            deleteTempFiles(file, context)
        }
    }

    override fun retryDownload(file: StructureDownFile, context: Context) {
        val filePath = File(file.downloadTo + '/' + file.fileName)
        if (filePath.exists()) {
            filePath.delete()
        }
        file.downloadState = DownloadStatusState.DOWNLOADING
        file.bytesCopied = 0
    }
}