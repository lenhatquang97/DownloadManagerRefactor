package com.quangln2.downloadmanagerrefactor.data.database

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.converter.Converters
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class DownloadDao {
    companion object {
        const val DOWNLOAD_LIST = "download_list"
    }

    fun getAll(context: Context): Flow<List<StructureDownFile>> = flow {
        val it = DownloadDataStore.getDownloadList(context).first()
        val result = if (it.isEmpty()) {
            emptyList<StructureDownFile>()
        } else {
            Converters.convertDownloadList(it)
        }
        emit(result)
    }

    suspend fun insert(file: StructureDownFile, context: Context) {
        val it = DownloadDataStore.getDownloadList(context).first()
        val list = if (it.isEmpty()) mutableListOf<StructureDownFile>() else Converters.convertDownloadList(it)
        list.add(file)
        CoroutineScope(Dispatchers.IO).launch {
            DownloadDataStore.setDownloadList(context, Converters.convertDownloadList(list))
        }
    }

    suspend fun update(file: StructureDownFile, context: Context) {
        val str = DownloadDataStore.getDownloadList(context).first()
        if (str.isNotEmpty()) {
            val list = Converters.convertDownloadList(str)
            for (i in list.indices) {
                if (list[i].id == file.id) {
                    list[i] = file.copy()
                    break
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                DownloadDataStore.setDownloadList(context, Converters.convertDownloadList(list))
            }
        }

    }

    suspend fun delete(file: StructureDownFile, context: Context) {
        val str = DownloadDataStore.getDownloadList(context).first()
        if (str.isNotEmpty()) {
            val list = Converters.convertDownloadList(str)
            for (i in list.indices) {
                if (list[i].id == file.id) {
                    list.removeAt(i)
                    break
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                DownloadDataStore.setDownloadList(context, Converters.convertDownloadList(list))
            }
        }

    }

     fun doesDownloadLinkExist(downloadLink: String, context: Context): Flow<Boolean> = flow {
         val str = DownloadDataStore.getDownloadList(context).first()
         if (str.isEmpty()) {
             println("B")
             emit(false)
         }
         val list = Converters.convertDownloadList(str)
         for (i in list.indices) {
             if (list[i].downloadLink == downloadLink && (list[i].downloadState == DownloadStatusState.DOWNLOADING || list[i].downloadState == DownloadStatusState.COMPLETED)) {
                 emit(true)
             }
         }
         emit(false)
    }

}