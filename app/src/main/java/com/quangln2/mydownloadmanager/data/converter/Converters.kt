package com.quangln2.mydownloadmanager.data.converter

import android.net.Uri
import androidx.room.TypeConverter
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState

class Converters {
    @TypeConverter
    fun fromDownloadState(value: DownloadStatusState): String{
        return value.toString()
    }
    @TypeConverter
    fun fromUri(value: Uri?): String{
        if(value == null){
            return ""
        }
        return value.path!!
    }
}