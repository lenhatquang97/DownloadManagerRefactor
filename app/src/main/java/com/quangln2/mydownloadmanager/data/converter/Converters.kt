package com.quangln2.mydownloadmanager.data.converter

import androidx.room.TypeConverter
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState

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
}