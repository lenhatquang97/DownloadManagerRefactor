package com.quangln2.mydownloadmanager.data.repository
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.annotation.WorkerThread
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.Dispatchers
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import kotlinx.coroutines.flow.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class DefaultDownloadRepository(private val downloadDao: DownloadDao): DownloadRepository {
    val downloadList: Flow<List<StrucDownFile>> = downloadDao.getAll()

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


    //TODO: Need to fix this bug
    override fun fetchDownloadInfo(file: StrucDownFile): StrucDownFile {
        val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.doOutput = true
        file.fileName = URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
        file.mimeType = if(connection.contentType == null) getMimeType(file.downloadLink) else connection.contentType
        file.size = connection.contentLength.toLong()
        file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
        connection.disconnect()
        return file


    }
    override fun getBytesFromExistingFile(file: StrucDownFile, context: Context): Long {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val resolver = context.contentResolver
            val selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?"
            val selectionArgs = arrayOf(file.fileName)
            val cursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null)
            if(cursor != null && cursor.count > 0){
                while(cursor.moveToNext()){
                    println("BYTES FROM EXISTING FILE: " + cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)).toString())
                    return cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE))
                }
            }
        } else {
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName
            val fileOpen = File(filePath)
            if(fileOpen.exists()){
                val fileSize = fileOpen.length()
                println("BYTES FROM EXISTING FILE: " + fileSize.toString())
                return fileSize
            }
        }
        return 0L
    }

    override fun writeToFileAPI29Above(file: StrucDownFile, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.downloadLink))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?"
            val selectionArgs = arrayOf(file.fileName)
            val cursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null)
            if(cursor != null && cursor.count <= 0 || file.uri == null){
                if(file.fileName.contains(".")){
                    //get substring of extension
                    val extension = file.fileName.substring(file.fileName.lastIndexOf("."))
                    //get name without extension
                    val name = file.fileName.substring(0, file.fileName.lastIndexOf("."))
                    file.fileName = name + "_" + UUID.randomUUID().toString().substring(0,4) + extension
                } else {
                    file.fileName = file.fileName + "_" + UUID.randomUUID().toString().substring(0,4)
                }
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                file.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                println(file.uri)
            }
        }

    }
    override fun writeToFileAPI29Below(file: StrucDownFile){
        if(file.fileName.contains(".")){
            //get substring of extension
            val extension = file.fileName.substring(file.fileName.lastIndexOf("."))
            //get name without extension
            val name = file.fileName.substring(0, file.fileName.lastIndexOf("."))
            file.fileName = name + "_" + UUID.randomUUID().toString().substring(0,4) + extension
        } else {
            file.fileName = file.fileName + "_" + UUID.randomUUID().toString().substring(0,4)
        }
        file.downloadTo = (if(file.downloadTo.isNullOrEmpty()) Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath else file.downloadTo) + "/" + file.fileName


    }


    override fun downloadAFile(file: StrucDownFile, context: Context): Flow<Int> = flow {
        val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
        if(connection != null){
            connection.setRequestProperty("Range", "bytes=${file.bytesCopied}-")
            connection.doInput = true
            connection.doOutput = true
        }
        val inp = BufferedInputStream(connection.inputStream)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if(file.uri == null){
                return@flow
            }
            val out = context.contentResolver.openOutputStream(file.uri!!,"wa")
            if (out != null) {
                val data = ByteArray(1024)
                var x = inp.read(data,0,1024)
                while(x >= 0){
                    out.write(data,0,x)
                    file.bytesCopied += x
                    val percent = file.bytesCopied.toFloat() / file.size.toFloat() * 100.0
                    emit(percent.toInt())
                    if(file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED){
                        break
                    }
                    x = inp.read(data,0,1024)
                }
            }
        } else {
            val fos = if (file.bytesCopied == 0L) FileOutputStream(file.downloadTo) else FileOutputStream(file.downloadTo, true)
            val data = ByteArray(1024)
            var x = inp.read(data,0,1024)
            while(x >= 0){
                fos.write(data,0,x)
                file.bytesCopied += x
                val percent = file.bytesCopied.toFloat() / file.size.toFloat() * 100.0
                emit(percent.toInt())
                if(file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED){
                    break
                }
                x = inp.read(data,0,1024)
            }
        }
        connection.disconnect()
    }.flowOn(Dispatchers.IO)


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

    override fun retryDownload(file: StrucDownFile,context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val resolver = context.contentResolver
            if(file.uri != null){
                resolver.delete(file.uri!!,null,null)
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.downloadLink))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            file.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val filePath = File(file.downloadTo)
            if(filePath.exists()){
                filePath.delete()
            }
            file.uri = null
        }
        file.bytesCopied = 0
        file.downloadState = DownloadStatusState.DOWNLOADING
    }
    override fun queueDownload(file: StrucDownFile) {
        file.downloadState = DownloadStatusState.QUEUED

    }

    override suspend fun copyFile() {

    }

    override suspend fun openDownloadFile() {

    }



}
