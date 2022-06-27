package com.quangln2.mydownloadmanager.data.model

import android.net.Uri
import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState

data class StrucDownFile (
    private val id: Int,
    var downloadLink: String,
    var downloadTo: String,

    var kindOf: String,
    var size: Long,
    var bytesCopied: Long,
    var downloadState: DownloadStatusState,
    var mimeType: String,

    var fileName: String,
    var uri: Uri? = null,
){

    fun convertToSizeUnit(): String{
        if (size < ConstantClass.KB){
            return size.toString() + "B"
        }
        return if (size >= ConstantClass.KB && size < ConstantClass.MB){
            val sizeKB = size.toFloat() / 1024.0
            String.format("%.2f", sizeKB) + "KB"
        } else if (size >= ConstantClass.MB && size < ConstantClass.GB){
            val sizeMB = size.toFloat() / 1024.0 / 1024.0
            String.format("%.2f", sizeMB) + "MB"
        } else {
            val sizeGB = size.toFloat() / 1024.0 / 1024.0 / 1024.0
            String.format("%.2f", sizeGB) + "GB"
        }
    }
    fun getCategory() = kindOf
}