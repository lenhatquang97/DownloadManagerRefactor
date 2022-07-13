package com.quangln2.mydownloadmanager.data.datasource

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile

interface LocalDataSource {
    suspend fun deletePermanently(file: StrucDownFile, context: Context)
    fun writeToFileAPI29Above(file: StrucDownFile, context: Context)
    fun writeToFileAPI29Below(file: StrucDownFile)
    suspend fun copyFile()
    fun openDownloadFile(item: StrucDownFile, context: Context)
}