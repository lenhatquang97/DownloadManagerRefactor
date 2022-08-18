package com.quangln2.downloadmanagerrefactor.util

import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass


class LogicUtil {
    companion object {
        fun calculateDownloadSpeed(seconds: Double, startBytes: Long, endBytes: Long): Double {
            return ((endBytes.toDouble() - startBytes.toDouble()) / ConstantClass.MB) / seconds
        }

        fun roundSize(value: Double): String {
            if (value >= 1.0) {
                return String.format("%.2f", value) + " MB/s"
            }
            return String.format("%.2f", value * 1000) + " KB/s"
        }

        fun cutFileName(fileName: String): String {
            return if (fileName.length < 25) fileName else fileName.substring(
                0,
                10
            ) + "..." + fileName.substring(fileName.length - 10, fileName.length)
        }

    }
}