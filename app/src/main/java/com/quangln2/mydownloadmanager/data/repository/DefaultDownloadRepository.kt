package com.quangln2.mydownloadmanager.data.repository
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadingState
import com.quangln2.mydownloadmanager.data.model.downloadstatus.FailedState
import com.quangln2.mydownloadmanager.data.model.downloadstatus.PausedState
import com.quangln2.mydownloadmanager.data.model.downloadstatus.QueuedState
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*


class DefaultDownloadRepository: DownloadRepository {
    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) {
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


    //TODO: Need to fix this bug
    override fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile {
        val connection = URL(file.downloadLink).openConnection()
        connection.doInput = true
        connection.doOutput = true
        file.fileName = URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
        file.mimeType = if(connection.contentType == null) getMimeType(file.downloadLink) else connection.contentType
        file.size = connection.contentLength.toLong()
        file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
        println("file.mimeType: ${file.mimeType}")
        return file


    }
    override fun writeToFileAPI29Above(file: StrucDownFile, context: Context) {
        val connection = URL(file.downloadLink).openConnection()
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


    override fun downloadAFile(file: StrucDownFile, context: Context): Flow<Int> = flow {
        val connection = URL(file.downloadLink).openConnection()
        if(connection != null){
            connection.setRequestProperty("Range", "bytes=${file.bytesCopied}-")
            connection.doInput = true
            connection.doOutput = true
        }
        val inp = BufferedInputStream(connection?.getInputStream())
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            writeToFileAPI29Above(file, context)
            val out = context.contentResolver.openOutputStream(file.uri!!,"wa")
            if (out != null) {
                val data = ByteArray(1024)
                var x = inp.read(data,0,1024)
                while(x >= 0){
                    out.write(data,0,x)
                    file.bytesCopied += x
                    val percent = file.bytesCopied.toFloat() / file.size.toFloat() * 100.0
                    println(percent)
                    emit(percent.toInt())
                    if(file.downloadState is PausedState || file.downloadState is FailedState){
                        break
                    }
                    x = inp.read(data,0,1024)
                }
            }
        } else {
            val downloadPath = writeToFileAPI29Below(file)
            val fos = if (file.bytesCopied == 0L) FileOutputStream(downloadPath) else FileOutputStream(downloadPath, true)
            val data = ByteArray(1024)
            var x = inp.read(data,0,1024)
            while(x >= 0){
                fos.write(data,0,x)
                file.bytesCopied += x
                val percent = file.bytesCopied.toFloat() / file.size.toFloat() * 100.0
                println(percent)
                emit(percent.toInt())
                if(file.downloadState is PausedState || file.downloadState is FailedState){
                    break
                }
                x = inp.read(data,0,1024)
            }
        }
    }.flowOn(Dispatchers.IO)


    override fun resumeDownload(file: StrucDownFile) {
        file.downloadState = DownloadingState(0,0)
    }

    override fun pauseDownload(file: StrucDownFile) {
        file.downloadState = PausedState()
    }

    override fun stopDownload(file: StrucDownFile) {
        file.bytesCopied = 0
        file.downloadState = FailedState()
    }

    override fun retryDownload(file: StrucDownFile,context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val resolver = context.contentResolver
            if(file.uri != null){
                resolver.delete(file.uri!!,null,null)
            }
        } else {
            val filePath = File(writeToFileAPI29Below(file))
            if(filePath.exists()){
                filePath.delete()
            }
        }
        file.bytesCopied = 0
        file.uri = null
        file.downloadState = DownloadingState(0,0)
    }
    override fun queueDownload(file: StrucDownFile) {
        file.downloadState = QueuedState(10)

    }

    override suspend fun copyFile() {

    }

    override suspend fun openDownloadFile() {

    }


}
