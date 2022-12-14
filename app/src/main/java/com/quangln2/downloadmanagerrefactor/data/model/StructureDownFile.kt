package com.quangln2.downloadmanagerrefactor.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import java.io.Serializable

@Entity(tableName = "download_list")
data class StructureDownFile(

    @PrimaryKey var id: String,
    @ColumnInfo(name = "download_link") var downloadLink: String,
    @ColumnInfo(name = "download_to") var downloadTo: String,
    @ColumnInfo(name = "kind_of") var kindOf: String,
    @ColumnInfo(name = "size") var size: Long,

    @ColumnInfo(name = "bytes_copied") var bytesCopied: Long,
    @ColumnInfo(name = "download_state") var downloadState: DownloadStatusState,
    @ColumnInfo(name = "mime_type") var mimeType: String,
    @ColumnInfo(name = "file_name") var fileName: String,
    @ColumnInfo(name = "list_chunks") var listChunks: MutableList<FromTo>,
    @ColumnInfo(name = "chunk_names") var chunkNames: MutableList<String>,

    @Ignore var textProgressFormat: String = "Loading...",

    ) : Serializable {
    constructor() :
            this(
                "",
                "",
                "",
                "",
                -1,
                0,
                DownloadStatusState.DOWNLOADING,
                "",
                "",
                mutableListOf(),
                mutableListOf()
            )

    fun convertToSizeUnit(): String {
        if (size < ConstantClass.KB) {
            return size.toString() + "B"
        }
        return if (size >= ConstantClass.KB && size < ConstantClass.MB) {
            val sizeKB = size.toFloat() / 1024.0
            String.format("%.2f", sizeKB) + "KB"
        } else if (size >= ConstantClass.MB && size < ConstantClass.GB) {
            val sizeMB = size.toFloat() / 1024.0 / 1024.0
            String.format("%.2f", sizeMB) + "MB"
        } else {
            val sizeGB = size.toFloat() / 1024.0 / 1024.0 / 1024.0
            String.format("%.2f", sizeGB) + "GB"
        }
    }

    fun convertBytesCopiedToSizeUnit(): String {
        if (bytesCopied < ConstantClass.KB) {
            return bytesCopied.toString() + "B"
        }
        return if (bytesCopied >= ConstantClass.KB && bytesCopied < ConstantClass.MB) {
            val sizeKB = bytesCopied.toFloat() / 1024.0
            String.format("%.2f", sizeKB) + "KB"
        } else if (bytesCopied >= ConstantClass.MB && bytesCopied < ConstantClass.GB) {
            val sizeMB = bytesCopied.toFloat() / 1024.0 / 1024.0
            String.format("%.2f", sizeMB) + "MB"
        } else {
            val sizeGB = bytesCopied.toFloat() / 1024.0 / 1024.0 / 1024.0
            String.format("%.2f", sizeGB) + "GB"
        }
    }
}