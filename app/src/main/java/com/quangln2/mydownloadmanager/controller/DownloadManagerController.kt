package com.quangln2.mydownloadmanager.controller

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object DownloadManagerController {
    var downloadListSchema: LiveData<List<StrucDownFile>>? = null

    private var _inputItem = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    private val inputItem: LiveData<StrucDownFile> get() = _inputItem

    private var _fetchedFileInfo = MutableLiveData<StrucDownFile>()
    val fetchedFileInfo : LiveData<StrucDownFile> get() = _fetchedFileInfo

    var _downloadList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

    private var _progressFile = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val progressFile: LiveData<StrucDownFile> get() = _progressFile


    fun addNewDownloadInfo(context: Context, url: String, downloadTo: String){
        if(_inputItem.value != null){
            ExternalUse.addNewDownloadInfo(context).invoke(url, downloadTo, _inputItem.value!!)
        }
    }

    fun fetchDownloadFileInfo(context: Context){
        val file = inputItem.value
        CoroutineScope(Dispatchers.IO).launch{
            if(file != null){
                ExternalUse.fetchDownloadInfo(context).invoke(file)
                _fetchedFileInfo.postValue(file)
            }
        }
    }

    fun downloadAFile(context: Context) {
        val file = _fetchedFileInfo.value
        if(file != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ExternalUse.writeToFileAPI29AboveUseCase(context)(file, context)
            } else {
                ExternalUse.writeToFileAPI29BelowUseCase(context)(file)
            }

            val currentList = downloadList.value
            if(currentList != null){
                if(ExternalUse.howManyFileDownloading < 3){
                    currentList.add(file.copy(downloadState = DownloadStatusState.DOWNLOADING))
                    ExternalUse.howManyFileDownloading += 1
                } else {
                    currentList.add(file.copy(downloadState = DownloadStatusState.QUEUED))
                }
                _downloadList.postValue(currentList)

            }
            CoroutineScope(Dispatchers.IO).launch{
                if(ExternalUse.howManyFileDownloading < 3) {
                    ExternalUse.insertToListUseCase(context)(file.copy(downloadState = DownloadStatusState.DOWNLOADING))
                } else {
                    ExternalUse.insertToListUseCase(context)(file.copy(downloadState = DownloadStatusState.QUEUED))
                }
            }
            val addedFile = currentList?.last()
            if (addedFile != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    ExternalUse.downloadAFileUseCase(context)(addedFile, context).collect {
                        _progressFile.value = it
                    }
                }
            }
        }
    }
    fun pause(id: String){
        val currentList = downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.PAUSED
            _progressFile.value = currentFile
        }
    }
    fun resume(context: Context, id: String){
        val currentList = downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            val doesFileExist = ExternalUse.isFileExistingUseCase(context).invoke(currentFile, context)
            if(doesFileExist){
                currentFile.bytesCopied = ExternalUse.getBytesFromExistingFileUseCase(context)(currentFile, context)
                currentFile.downloadState = DownloadStatusState.DOWNLOADING
                CoroutineScope(Dispatchers.Main).launch {
                    ExternalUse.downloadAFileUseCase(context)(currentFile, context).collect {
                        _progressFile.value = it
                    }
                }
            } else {
                currentFile.downloadState = DownloadStatusState.FAILED
                _progressFile.value = currentFile
            }
        }
    }

    fun retry(context: Context, item: StrucDownFile){
        ExternalUse.retryDownloadUseCase(context)(item, context)
        CoroutineScope(Dispatchers.Main).launch {
            ExternalUse.downloadAFileUseCase(context)(item, context).collect {
                _progressFile.value = it
            }
        }
    }
    fun open(context: Context, item: StrucDownFile){
        val doesFileExist = ExternalUse.isFileExistingUseCase(context).invoke(item, context)
        if(doesFileExist){
            ExternalUse.openDownloadFileUseCase(context)(item,context)
        }
    }

}