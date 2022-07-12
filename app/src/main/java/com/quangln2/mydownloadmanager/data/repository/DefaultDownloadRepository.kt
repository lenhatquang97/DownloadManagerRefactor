package com.quangln2.mydownloadmanager.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.quangln2.mydownloadmanager.BuildConfig
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class DefaultDownloadRepository(private val downloadDao: DownloadDao): DownloadRepository {

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val resolver = context.contentResolver
            if(file.uri != null){
                val rowsDeleted = resolver.delete(file.uri!!,null,null)
                if(rowsDeleted <= 0){
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "File not found so we'll delete from list", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val filePath = File(file.downloadTo)
            if(filePath.exists()){
                filePath.delete()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "File not found so we'll delete from list", Toast.LENGTH_SHORT).show()
                }
            }
        }
        downloadDao.delete(file)
    }

    override fun isFileExisting(file: StrucDownFile, context: Context): Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val filePath = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName)
            if(!filePath.exists()){
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            val filePath = File(file.downloadTo)
            if(!filePath.exists()){
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StrucDownFile) {
        file.id = UUID.randomUUID().toString()
        file.downloadLink = url
        file.downloadTo = downloadTo
        file.bytesCopied = 0
        println(file.downloadLink)
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
        try{
            val responseCode = connection.responseCode
            return if(responseCode == 200){
                file.fileName = URLUtil.guessFileName(file.downloadLink, null, connection.contentType)
                file.mimeType = if(connection.contentType == null) getMimeType(file.downloadLink) else connection.contentType
                file.size = connection.contentLength.toLong()
                file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
                connection.disconnect()
                file
            } else{
                val initFile = ServiceLocator.initializeStrucDownFile()
                file.fileName = initFile.fileName
                file.size = initFile.size
                initFile
            }
        } catch (e: Exception){
            val initFile = ServiceLocator.initializeStrucDownFile()
            file.fileName = initFile.fileName
            file.size = initFile.size
            return initFile
        }

    }
    override fun getBytesFromExistingFile(file: StrucDownFile, context: Context): Long {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val resolver = context.contentResolver
            val selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?"
            val selectionArgs = arrayOf(file.fileName)
            val cursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null)
            if(cursor != null && cursor.count > 0){
                while(cursor.moveToNext()){
                    return cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE))
                }
            }
        } else {
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName
            val fileOpen = File(filePath)
            if(fileOpen.exists()) {
                return fileOpen.length()
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
        file.downloadTo = (file.downloadTo.ifEmpty { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath }) + "/" + file.fileName


    }
    override fun downloadAFile(file: StrucDownFile, context: Context): Flow<StrucDownFile> = flow {
        try{
            val connection = URL(file.downloadLink).openConnection() as HttpURLConnection
            connection.setRequestProperty("Range", "bytes=${file.bytesCopied}-")
            connection.doInput = true
            connection.doOutput = true
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
                        emit(file)
                        if(file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED){
                            out.close()
                            connection.disconnect()
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
                    emit(file)
                    if(file.downloadState == DownloadStatusState.PAUSED || file.downloadState == DownloadStatusState.FAILED){
                        fos.close()
                        connection.disconnect()
                        break
                    }
                    x = inp.read(data,0,1024)
                }
            }
        } catch(e: Exception){
            emit(file.copy(downloadState = DownloadStatusState.FAILED))
        }

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



    override fun openDownloadFile(item: StrucDownFile, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.setDataAndType(item.uri, item.mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            val file = File(item.downloadTo)
            if(file.exists()){
                val uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                intent.setDataAndType(uri, item.mimeType)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            Toast.makeText(context, "There is no application to open this file", Toast.LENGTH_SHORT).show()
        }
        else{
            context.startActivity(intent)
        }
    }



}
