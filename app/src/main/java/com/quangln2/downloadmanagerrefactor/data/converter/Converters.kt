package com.quangln2.downloadmanagerrefactor.data.converter

import androidx.room.TypeConverter
import com.quangln2.downloadmanagerrefactor.data.model.FromTo
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState

class Converters {
    @TypeConverter
    fun convertDownloadState(value: String): DownloadStatusState {
        return when (value) {
            DownloadStatusState.DOWNLOADING.toString() -> DownloadStatusState.DOWNLOADING
            DownloadStatusState.PAUSED.toString() -> DownloadStatusState.PAUSED
            DownloadStatusState.COMPLETED.toString() -> DownloadStatusState.COMPLETED
            DownloadStatusState.FAILED.toString() -> DownloadStatusState.FAILED
            else -> DownloadStatusState.QUEUED
        }
    }

    @TypeConverter
    fun convertDownloadState(value: DownloadStatusState): String {
        return value.toString()
    }

    @TypeConverter
    fun convertListChunks(value: MutableList<FromTo>?): String {
        if (value != null) {
            return value.joinToString("@") { "${it.from} ${it.to} ${it.curr}" }
        }
        return ""
    }

    @TypeConverter
    fun convertListChunks(value: String): MutableList<FromTo> {
        if (value.isNotEmpty()) {
            return value.split("@").map {
                val split = it.split(" ")
                FromTo(split[0].toLong(), split[1].toLong(), split[2].toLong())
            }.toMutableList()
        }
        return mutableListOf()
    }

    @TypeConverter
    fun convertChunkNames(value: MutableList<String>): String {
        return value.joinToString("?")
    }

    @TypeConverter
    fun convertChunkNames(value: String): MutableList<String> {
        return value.split("?").toMutableList()
    }
}