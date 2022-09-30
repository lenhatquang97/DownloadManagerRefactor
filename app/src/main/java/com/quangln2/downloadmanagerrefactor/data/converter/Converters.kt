package com.quangln2.downloadmanagerrefactor.data.converter

import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.model.FromTo
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState

class Converters {
    companion object {
        fun convertDownloadList(ls: List<StructureDownFile>): String {
            val arr = mutableListOf<String>()
            for (item in ls) {
                arr.add(item.convertToJsonStringForKeyValueDB())
            }
            return arr.joinToString(separator = ConstantClass.DOWNLOAD_LIST_SEPARATOR) { it }
        }

        fun convertDownloadList(json: String): MutableList<StructureDownFile> {
            val arr = mutableListOf<StructureDownFile>()
            val ls = json.split(ConstantClass.DOWNLOAD_LIST_SEPARATOR)
            for (item in ls) {
                arr.add(StructureDownFile.convertStringToClass(item))
            }
            return arr
        }

    }

    fun convertDownloadState(value: String): DownloadStatusState {
        return when (value) {
            DownloadStatusState.DOWNLOADING.toString() -> DownloadStatusState.DOWNLOADING
            DownloadStatusState.PAUSED.toString() -> DownloadStatusState.PAUSED
            DownloadStatusState.COMPLETED.toString() -> DownloadStatusState.COMPLETED
            DownloadStatusState.FAILED.toString() -> DownloadStatusState.FAILED
            else -> DownloadStatusState.QUEUED
        }
    }

    fun convertDownloadState(value: DownloadStatusState): String {
        return value.toString()
    }

    fun convertListChunks(value: MutableList<FromTo>?): String {
        if (value != null) {
            return value.joinToString(ConstantClass.DOWNLOAD_LIST_CHUNK_SEPARATOR) { "${it.from} ${it.to} ${it.curr}" }
        }
        return ""
    }

    fun convertListChunks(value: String): MutableList<FromTo> {
        if (value.isNotEmpty()) {
            return value.split(ConstantClass.DOWNLOAD_LIST_CHUNK_SEPARATOR).map {
                val split = it.split(" ")
                FromTo(split[0].toLong(), split[1].toLong(), split[2].toLong())
            }.toMutableList()
        }
        return mutableListOf()
    }

    fun convertChunkNames(value: MutableList<String>): String {
        if (value.size == 0) return ""
        return value.joinToString(ConstantClass.DOWNLOAD_CHUNK_NAMES_SEPARATOR)
    }

    fun convertChunkNames(value: String): MutableList<String> {
        if (value.isEmpty()) return mutableListOf()
        return value.split(ConstantClass.DOWNLOAD_CHUNK_NAMES_SEPARATOR).toMutableList()
    }

}