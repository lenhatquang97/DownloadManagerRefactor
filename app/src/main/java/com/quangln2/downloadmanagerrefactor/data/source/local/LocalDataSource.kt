package com.quangln2.downloadmanagerrefactor.data.source.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile

interface LocalDataSource {
    suspend fun deletePermanently(file: StructureDownFile, context: Context)
    fun writeToFile(file: StructureDownFile)
    fun openDownloadFile(item: StructureDownFile, context: Context)
    suspend fun insert(file: StructureDownFile)
    suspend fun update(file: StructureDownFile)
    suspend fun deleteFromDatabase(StructureDownFile: StructureDownFile)
    suspend fun doesDownloadLinkExist(file: StructureDownFile): Boolean
    fun vibratePhone(context: Context)
}