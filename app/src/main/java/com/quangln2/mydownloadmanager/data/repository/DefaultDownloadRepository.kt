package com.quangln2.mydownloadmanager.data.repository
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.URLUtil
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import kotlinx.coroutines.flow.Flow
import java.util.*

class DefaultDownloadRepository: DownloadRepository {
    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) {
        file.downloadLink = url
        file.downloadTo = downloadTo
    }


    //TODO: Need to fix this bug
    override fun fetchDownloadInfo(file: StrucDownFile): Flow<StrucDownFile> = flow {
        val connection = URL(file.downloadLink).openConnection()
        connection.doInput = true
        connection.doOutput = true
        file.fileName = URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
        file.mimeType = connection.contentType
        file.size = connection.contentLength.toLong()
        emit(file)
        delay(200)
    }
    override fun writeToFileAPI29Above(file: StrucDownFile, context: Context) = CoroutineScope(Dispatchers.IO).async {
        val defer = async {
            URL(file.downloadLink).openConnection()
        }
        val connection = defer.await()
        connection.doInput = true
        connection.doOutput = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, connection.contentType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ?"
            val selectionArgs = arrayOf(file.fileName)
            val cursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null)
             if(cursor!!.count <= 0) {
                file.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else if(file.uri == null){
                file.fileName = file.fileName + "_" + UUID.randomUUID().toString().substring(0,4)
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                file.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            }
        }
    }
    override fun writeToFileAPI29Below(file: StrucDownFile): String{
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName
    }


    override fun downloadAFile(file: StrucDownFile, context: Context) = CoroutineScope(Dispatchers.IO).launch {
        val defer = async {
            URL(file.downloadLink).openConnection()
        }
        val connection = defer.await()
        connection.setRequestProperty("Range", "bytes=${file.bytesCopied}-")
        connection.doInput = true
        connection.doOutput = true
        val inp = BufferedInputStream(connection.getInputStream())
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            writeToFileAPI29Above(file, context).await()
            val out = context.contentResolver.openOutputStream(file.uri!!,"wa")
            if (out != null) {
                writeStreamIO(inp, out, file).await()
            }
        } else {
            val downloadPath = writeToFileAPI29Below(file)
            var fos = if (file.bytesCopied == 0L) FileOutputStream(downloadPath) else FileOutputStream(downloadPath, true)
            writeStreamIO(inp, fos, file).await()
        }
    }
    private fun writeStreamIO(inp: BufferedInputStream, out: OutputStream, file: StrucDownFile) = CoroutineScope(Dispatchers.IO).async {
        val data = ByteArray(1024)
        var x = 0
        x = inp.read(data,0,1024)
        while(x >= 0){
            out?.write(data,0,x)
            file.bytesCopied += x
            println(file.bytesCopied)
            if(file.downloadState is PausedState || file.downloadState is FailedState){
                return@async
            }
            x = inp.read(data,0,1024)
        }
    }



    override fun resumeDownload(file: StrucDownFile) {
        file.downloadState = DownloadingState(0,0)
    }

    override fun pauseDownload(file: StrucDownFile) {
        file.downloadState = PausedState()
    }

    override fun stopDownload(file: StrucDownFile) {
        file.downloadState = FailedState()
    }

    override suspend fun retryDowwnload(file: StrucDownFile, context: Context) {
        file.downloadState = DownloadingState(0,0)
        downloadAFile(file, context)
    }
    override fun queueDownload(file: StrucDownFile) {
        file.downloadState = QueuedState(10)

    }

    override suspend fun copyFile() {

    }

    override suspend fun openDownloadFile() {

    }

}
