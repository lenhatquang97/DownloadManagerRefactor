package com.quangln2.mydownloadmanager.util

import com.quangln2.mydownloadmanager.data.constants.ConstantClass

class LogicUtil {
    companion object{
        fun calculateDownloadSpeed(seconds: Double, startBytes: Long, endBytes: Long): String {
            val kbPerSecond = ((endBytes.toDouble() - startBytes.toDouble()) / ConstantClass.MB) / seconds
            return String.format("%.2f MB/s", kbPerSecond)
        }
        fun cutFileName(fileName: String): String{
            return if(fileName.length < 25) fileName else fileName.substring(0,10) + "..." + fileName.substring(fileName.length - 10, fileName.length)
        }
    }
}