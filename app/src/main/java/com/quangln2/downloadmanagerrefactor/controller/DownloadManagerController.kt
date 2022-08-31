package com.quangln2.downloadmanagerrefactor.controller

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quangln2.downloadmanagerrefactor.DownloadManagerApplication
import com.quangln2.downloadmanagerrefactor.ServiceLocator
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.domain.local.InsertToListUseCase
import com.quangln2.downloadmanagerrefactor.domain.local.UpdateToListUseCase
import com.quangln2.downloadmanagerrefactor.domain.local.WriteToFileUseCase
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DownloadManagerController {

    var filterName = "All"
    var numberOfHTTPChunks = 5
    var numberOfSocketChunks = 1

    var _inputItem =
        MutableLiveData<StructureDownFile>().apply {
            value = ServiceLocator.initializeStructureDownFile()
        }
    val inputItem: LiveData<StructureDownFile> get() = _inputItem


    var _downloadList =
        MutableLiveData<MutableList<StructureDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StructureDownFile>> get() = _downloadList

    var _filterList =
        MutableLiveData<MutableList<StructureDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StructureDownFile>> get() = _filterList

    var _progressFile =
        MutableLiveData<StructureDownFile>().apply {
            value = StructureDownFile()
        }
    val progressFile: LiveData<StructureDownFile> get() = _progressFile

    val speedController = MutableLiveData<MutableMap<String, DownloadSpeedController>>().apply {
        value = mutableMapOf()
    }

    val newItem = MutableLiveData<StructureDownFile>().apply {
        value = StructureDownFile()
    }


    fun findNextQueueDownloadFile(context: Context) {
        val currentList = downloadList.value
        if (currentList != null) {
            val index = currentList.indexOfFirst { it.downloadState == DownloadStatusState.QUEUED }
            if (index != -1) {
                currentList[index] =
                    currentList[index].copy(downloadState = DownloadStatusState.DOWNLOADING)
                _downloadList.postValue(currentList)
                val intent = Intent(context, DownloadService::class.java)
                DownloadManagerController.newItem.value = currentList[index]
                intent.putExtra("command", "dequeue")
                context.startService(intent)

            }
        }
    }

    fun addToQueueList(file: StructureDownFile) {
        val currentList = downloadList.value
        if (currentList != null) {
            currentList.add(file.copy(downloadState = DownloadStatusState.QUEUED))
            _downloadList.postValue(currentList)
        }
        CoroutineScope(Dispatchers.IO).launch {
            InsertToListUseCase(DownloadManagerApplication.downloadRepository)(
                file.copy(
                    downloadState = DownloadStatusState.QUEUED
                )
            )
        }
    }

    fun addToDownloadList(file: StructureDownFile) {
        val currentList = downloadList.value
        if (currentList != null) {
            val availableValue = currentList.find { it.id == file.id }
            if (availableValue != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    UpdateToListUseCase(DownloadManagerApplication.downloadRepository)(
                        file.copy(
                            downloadState = DownloadStatusState.DOWNLOADING
                        )
                    )
                }
                return
            } else {
                currentList.add(file.copy(downloadState = DownloadStatusState.DOWNLOADING))
                _downloadList.postValue(currentList.toMutableList())
                CoroutineScope(Dispatchers.IO).launch {
                    InsertToListUseCase(DownloadManagerApplication.downloadRepository)(
                        file.copy(
                            downloadState = DownloadStatusState.DOWNLOADING
                        )
                    )
                }
                return
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            InsertToListUseCase(DownloadManagerApplication.downloadRepository)(
                file.copy(
                    downloadState = DownloadStatusState.DOWNLOADING
                )
            )
        }
    }

    fun createFileAgain(file: StructureDownFile) {
        if (file.downloadTo.isEmpty()) {
            WriteToFileUseCase(DownloadManagerApplication.downloadRepository)(file)
        }
    }


}