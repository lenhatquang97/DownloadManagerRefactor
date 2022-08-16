package com.quangln2.downloadmanagerrefactor.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import java.io.File

class DownloadUtil {
    companion object {
        fun getMimeType(url: String?): String {
            var type = "*/*"
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
            }
            return type
        }

        fun isFileExisting(file: StructureDownFile, context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val filePath =
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName)
                if (!filePath.exists()) {
                    return false
                }
            } else {
                val filePath = File(file.downloadTo + '/' + file.fileName)
                if (!filePath.exists()) {
                    return false
                }
            }
            return true
        }

        fun getBytesFromExistingFile(file: StructureDownFile, context: Context): Long {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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
                if (cursor != null && cursor.count > 0) {
                    while (cursor.moveToNext() && cursor.getColumnIndex(MediaStore.MediaColumns.SIZE) != -1) {
                        val result =
                            cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE))
                        cursor.close()
                        return result
                    }
                }
            } else {
                val filePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + file.fileName
                val fileOpen = File(filePath)
                if (fileOpen.exists()) {
                    return fileOpen.length()
                }
            }
            return 0L
        }

        fun isNetworkAvailable(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }
    }
}