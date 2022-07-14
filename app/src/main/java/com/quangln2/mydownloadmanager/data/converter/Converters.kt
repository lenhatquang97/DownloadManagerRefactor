package com.quangln2.mydownloadmanager.data.converter

import android.net.Uri
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

    @TypeConverter
    fun convertUri(value: Uri?): String {
        if (value == null) {
            return ""
        }
        return value.toString()
    }

    @TypeConverter
    fun convertUri(value: String): Uri {
        return Uri.parse(value)
    }
}