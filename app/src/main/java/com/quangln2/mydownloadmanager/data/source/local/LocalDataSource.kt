package com.quangln2.mydownloadmanager.data.source.local

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StructureDownFile

interface LocalDataSource {
    suspend fun deletePermanently(file: StructureDownFile, context: Context)
    fun writeToFileAPI29Above(file: StructureDownFile, context: Context)
    fun writeToFileAPI29Below(file: StructureDownFile)
    fun openDownloadFile(item: StructureDownFile, context: Context)
}