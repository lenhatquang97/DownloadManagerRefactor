package com.quangln2.downloadmanagerrefactor.data.source.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun deletePermanently(file: StructureDownFile, context: Context)
    fun writeToFile(file: StructureDownFile)
    fun openDownloadFile(item: StructureDownFile, context: Context)
    suspend fun insert(file: StructureDownFile, context: Context)
    suspend fun update(file: StructureDownFile, context: Context)
    suspend fun deleteFromDatabase(StructureDownFile: StructureDownFile, context: Context)
    fun doesDownloadLinkExist(file: StructureDownFile, context: Context): Flow<Boolean>
    fun vibratePhone(context: Context)
}