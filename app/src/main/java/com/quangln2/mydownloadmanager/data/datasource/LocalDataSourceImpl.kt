package com.quangln2.mydownloadmanager.data.datasource

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.quangln2.mydownloadmanager.BuildConfig
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.util.DownloadUtil.Companion.getMimeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class LocalDataSourceImpl : LocalDataSource {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun deletePermanently(file: StrucDownFile, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            if (file.uri != null) {
                val rowsDeleted = resolver.delete(
                    Uri.parse(file.uri!!), null, null)
                if (rowsDeleted <= 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "File not found so we'll delete from list",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            val filePath = File(file.downloadTo)
            if (filePath.exists()) {
                filePath.delete()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        "File not found so we'll delete from list",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun writeToFileAPI29Above(file: StrucDownFile, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file.downloadLink))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?"
            val selectionArgs = arrayOf(file.fileName)
            val cursor = resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.count <= 0 || file.uri == null) {
                if (file.fileName.contains(".")) {
                    //get substring of extension
                    val extension = file.fileName.substring(file.fileName.lastIndexOf("."))
                    //get name without extension
                    val name = file.fileName.substring(0, file.fileName.lastIndexOf("."))
                    file.fileName =
                        name + "_" + UUID.randomUUID().toString().substring(0, 4) + extension
                } else {
                    file.fileName =
                        file.fileName + "_" + UUID.randomUUID().toString().substring(0, 4)
                }
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.fileName)
                file.uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues).toString()
            }
        }

    }

    override fun writeToFileAPI29Below(file: StrucDownFile) {
        if (file.fileName.contains(".")) {
            val extension = file.fileName.substring(file.fileName.lastIndexOf("."))
            val name = file.fileName.substring(0, file.fileName.lastIndexOf("."))
            file.fileName = name + "_" + UUID.randomUUID().toString().substring(0, 4) + extension
        } else {
            file.fileName = file.fileName + "_" + UUID.randomUUID().toString().substring(0, 4)
        }
        file.downloadTo = (file.downloadTo.ifEmpty {
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).absolutePath
        }) + "/" + file.fileName


    }

    override suspend fun copyFile() {

    }


    override fun openDownloadFile(item: StrucDownFile, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.setDataAndType(Uri.parse(item.uri), item.mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            val file = File(item.downloadTo)
            if (file.exists()) {
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
            Toast.makeText(context, "There is no application to open this file", Toast.LENGTH_SHORT)
                .show()
        } else {
            context.startActivity(intent)
        }
    }
}