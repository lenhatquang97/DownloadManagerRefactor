package com.quangln2.mydownloadmanager.data.model

import android.net.Uri
import androidx.room.*
import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState

@Entity(tableName ="download_list")
data class StrucDownFile (

    @PrimaryKey var id: String,
    @ColumnInfo(name="download_link") var downloadLink: String,
    @ColumnInfo(name="download_to") var downloadTo: String,
    @ColumnInfo(name="kind_of") var kindOf: String,
    @ColumnInfo(name="size") var size: Long,

    @Ignore var bytesCopied: Long,

    @ColumnInfo(name="download_state") var downloadState: DownloadStatusState,
    @ColumnInfo(name="mime_type") var mimeType: String,
    @ColumnInfo(name="file_name") var fileName: String,

    @ColumnInfo(name="uri") var uri: Uri? = null,
){
    constructor():
            this("", "", "", "", -1, 0, DownloadStatusState.DOWNLOADING, "", "", null)

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