package com.quangln2.downloadmanagerrefactor.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.os.Environment
import android.os.StatFs
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.*
import java.io.File

class DownloadUtil {
    companion object {
        fun checkAvailableSpace(): Long{
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            return bytesAvailable
        }

        fun isFileExisting(file: StructureDownFile, context: Context): Boolean {
            val filePath = File(file.downloadTo + '/' + file.fileName)
            return filePath.exists()
        }

        fun isFileExistingInFilesDir(fileName: String, context: Context): Boolean {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), fileName)
            return appSpecificExternalDir.exists()
        }
        fun sizeOfFilesDir(fileName: String, context: Context): Long {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), fileName)
            return appSpecificExternalDir.length()
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


        fun filterCategories(categories: String) {
            DownloadManagerController.filterName = categories
            val currentList = DownloadManagerController.downloadList.value
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            if (categories == "All") {
                scope.launch(Dispatchers.IO) {
                    DownloadManagerController._filterList.postValue(currentList?.toMutableList())
                }
                return
            }
            if (currentList != null) {
                scope.launch(Dispatchers.IO) {
                    val newList = currentList.filter { it.kindOf == categories }
                    DownloadManagerController._filterList.postValue(newList.toMutableList())
                }
                return
            }
        }
    }
}