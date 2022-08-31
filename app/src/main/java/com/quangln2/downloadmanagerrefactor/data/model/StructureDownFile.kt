package com.quangln2.downloadmanagerrefactor.data.model


import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.converter.Converters
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.data.source.protocol.HttpProtocol
import com.quangln2.downloadmanagerrefactor.data.source.protocol.ProtocolInterface
import org.json.JSONObject
import java.io.Serializable

data class StructureDownFile(

    var id: String,
    var downloadLink: String,
    var downloadTo: String,
    var kindOf: String,
    var size: Long,

    var bytesCopied: Long,
    var downloadState: DownloadStatusState,
    var mimeType: String,
    var fileName: String,


    var listChunks: MutableList<FromTo>,
    var chunkNames: MutableList<String>,
    var protocol: String = "HTTP",


    //Ignored
    var textProgressFormat: String = "Loading...",
    var protocolInterface: ProtocolInterface = HttpProtocol()

) : Serializable {
    companion object {
        fun convertStringToClass(jsonString: String): StructureDownFile {
            val converters = Converters()
            val jsonObject = JSONObject(jsonString)
            return StructureDownFile(
                jsonObject.getString("id"),
                jsonObject.getString("download_link"),
                jsonObject.getString("download_to"),
                jsonObject.getString("kind_of"),
                jsonObject.getLong("size"),
                jsonObject.getLong("bytes_copied"),
                converters.convertDownloadState(jsonObject.getString("download_state")),
                jsonObject.getString("mime_type"),
                jsonObject.getString("file_name"),
                converters.convertListChunks(jsonObject.getString("list_chunks")),
                converters.convertChunkNames(jsonObject.getString("chunk_names")),
                jsonObject.getString("protocol"),
            )
        }
    }

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

    fun convertToJsonStringForKeyValueDB(): String {
        val converters = Converters()
        val jsonObject = JSONObject()
        jsonObject.apply {
            put("id", id)
            put("download_link", downloadLink)
            put("download_to", downloadTo)
            put("kind_of", kindOf)
            put("size", size)
            put("bytes_copied", bytesCopied)
            put("download_state", converters.convertDownloadState(downloadState))
            put("mime_type", mimeType)
            put("file_name", fileName)
            put("list_chunks", converters.convertListChunks(listChunks))
            put("chunk_names", converters.convertChunkNames(chunkNames))
            put("protocol", protocol)
        }
        return jsonObject.toString()
    }


}