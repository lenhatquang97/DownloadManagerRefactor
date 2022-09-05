package com.quangln2.downloadmanagerrefactor.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DownloadUtil {
    companion object {
        //Related to storage
        fun checkAvailableSpace(): Long {
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

        //Check whether network is good
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

        //Filter with categories
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

        //Merge multiple file into one file
        fun combineFile(file: StructureDownFile, context: Context, chunkNumbers: Int) {
            try{
                val fout = FileOutputStream(file.downloadTo + "/" + file.fileName)
                (0 until chunkNumbers).forEach {
                    val fin =
                        FileInputStream(context.getExternalFilesDir(null)?.absolutePath + '/' + file.chunkNames[it])
                    fin.use { it ->
                        readByteByByte(it, fout)
                    }
                    fin.close()
                }
                fout.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        private fun readByteByByte(fin: FileInputStream, fout: FileOutputStream) {
            val bufferSize = 4 * 1024
            val data = ByteArray(bufferSize)
            var x = fin.read(data, 0, bufferSize)
            while (x >= 0) {
                fout.write(data, 0, x)
                x = fin.read(data, 0, bufferSize)
            }

        }
    }
}