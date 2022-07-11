package com.quangln2.mydownloadmanager.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.quangln2.mydownloadmanager.data.constants.ConstantClass


class LogicUtil {
    companion object{
        fun calculateDownloadSpeed(seconds: Double, startBytes: Long, endBytes: Long): Double {
            return ((endBytes.toDouble() - startBytes.toDouble()) / ConstantClass.MB) / seconds
        }
        fun cutFileName(fileName: String): String{
            return if(fileName.length < 25) fileName else fileName.substring(0,10) + "..." + fileName.substring(fileName.length - 10, fileName.length)
        }
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }
    }
}