package com.quangln2.downloadmanagerrefactor.data.source.remote

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.quangln2.downloadmanagerrefactor.ServiceLocator
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.numberOfChunks
import com.quangln2.downloadmanagerrefactor.data.model.FromTo
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class RemoteDataSourceImpl : RemoteDataSource {

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile) {
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

    override fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile {
        val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"
        )
        connection.doInput = true
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
                file.listChunks = (0 until numberOfChunks).map {
                    val tmp = file.size / numberOfChunks
                    val endVal = if (it == numberOfChunks - 1) file.size else tmp * (it + 1) - 1
                    FromTo(tmp * it, endVal, tmp * it)
                }.toMutableList()
                if (file.listChunks != null) {
                    file.chunkValues = file.listChunks?.map { it.from }!!.toMutableList()
                }
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

    override fun downloadAFileWithChunks(
        file: StructureDownFile,
        context: Context
    ): Flow<StructureDownFile> =
        channelFlow {
            try {
                withContext(Dispatchers.IO) {
                    (0 until numberOfChunks).map {
                        val deferred = async(Dispatchers.IO) {
                            val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
                            connection.setRequestProperty("Connection", "Keep-Alive")
                            connection.doInput = true
                            connection.connectTimeout = 5000

                            val from = if (file.chunkValues[it] == 0L) file.listChunks!![it].from
                            else file.chunkValues[it]
                            val to = file.listChunks!![it].to
                            if (from >= to) return@async
                            connection.setRequestProperty("Range", "bytes=${from}-${to}")
                            val inp = BufferedInputStream(connection.inputStream)
                            val filePath = File(file.downloadTo)
                            val raf = RandomAccessFile(filePath, "rws")
                            raf.seek(from)
                            val data = ByteArray(1024)
                            var x = inp.read(data, 0, 1024)
                            while (x >= 0) {
                                raf.write(data, 0, x)
                                file.chunkValues[it] += x.toLong()
                                file.bytesCopied = file.chunkValues.zip(file.listChunks!!).map {
                                    minOf(it.first, it.second.to) - it.second.from + 1
                                }.reduce { a, b -> a + b } - 1
                                if (file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED) {
                                    raf.close()
                                    connection.disconnect()
                                    break
                                }
                                x = inp.read(data, 0, 1024)
                                send(file)
                            }
                        }
                        deferred
                    }.awaitAll()
                    if (file.downloadState == DownloadStatusState.DOWNLOADING) {
                        send(file.copy(bytesCopied = file.size, downloadState = DownloadStatusState.COMPLETED))
                    }
                }
            } catch (e: Exception) {
                println(e)
                send(file.copy(downloadState = DownloadStatusState.FAILED))
            }
        }.debounce(100).flowOn(Dispatchers.IO)


    override fun resumeDownload(file: StructureDownFile, context: Context) {
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == file.id }
        if (index != null && index != -1) {
            val currentFile = currentList[index]
            val doesFileExist = DownloadUtil.isFileExisting(currentFile, context)
            if (doesFileExist) {
                currentFile.downloadState = DownloadStatusState.DOWNLOADING
            } else {
                currentFile.downloadState = DownloadStatusState.FAILED
                DownloadManagerController._progressFile.value = currentFile
            }
        }
    }

    override fun pauseDownload(file: StructureDownFile) {
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == file.id }
        if (index != null && index != -1) {
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.PAUSED
            DownloadManagerController._progressFile.value = currentFile
        }
    }

    override fun stopDownload(file: StructureDownFile) {
        file.bytesCopied = 0
        file.chunkValues = file.listChunks?.map { it.from }!!.toMutableList()
        file.downloadState = DownloadStatusState.FAILED
    }

    override fun retryDownload(file: StructureDownFile, context: Context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val filePath = File(file.downloadTo)
            if (filePath.exists()) {
                filePath.delete()
            }
            file.downloadTo = ""
            file.uri = null
        }
        file.downloadState = DownloadStatusState.DOWNLOADING
        file.bytesCopied = 0
    }

}