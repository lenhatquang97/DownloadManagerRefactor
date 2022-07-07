package com.quangln2.mydownloadmanager.controller

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.listener.ProgressCallback
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object DownloadManagerController {
    var downloadListSchema: LiveData<List<StrucDownFile>>? = null

    private var _inputItem = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val inputItem: LiveData<StrucDownFile> get() = _inputItem

    private var _fetchedFileInfo = MutableLiveData<StrucDownFile>()
    val fetchedFileInfo : LiveData<StrucDownFile> get() = _fetchedFileInfo

    var _downloadList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

    var _filterList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StrucDownFile>> get() = _filterList

    private var _progressFile = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val progressFile: LiveData<StrucDownFile> get() = _progressFile

    fun getDataFromDatabase(){
        downloadListSchema?.value?.let{
            _downloadList.value?.let { ls ->
                if(ls.size != 0){
                    _downloadList.value = it.toMutableList()
                    _filterList.value = it.toMutableList()
                }
            }
        }
    }

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

            val currentList = _downloadList.value
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
        val currentList = _downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.PAUSED
            _progressFile.value = currentFile
        }
    }
    fun resume(context: Context, id: String){
        val currentList = _downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.DOWNLOADING
            CoroutineScope(Dispatchers.Main).launch {
                ExternalUse.downloadAFileUseCase(context)(currentFile, context).collect {
                    _progressFile.value = it
                }
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
        ExternalUse.openDownloadFileUseCase(context)(item,context)
    }


    fun filterList(downloadStatusState: String){
        if(downloadStatusState == DownloadStatusState.ALL.toString()){
            _filterList.postValue(downloadList.value)
            return
        }
        val currentList = _downloadList.value
        if(currentList != null){
            val newList = currentList.filter { it.downloadState.toString() == downloadStatusState }
            _filterList.postValue(newList.toMutableList())
        }
    }

    fun filterCategories(categories: String){
        val currentList = _downloadList.value
        if(categories == "All"){
            _filterList.postValue(currentList)
            return
        }
        if(currentList != null){
            val newList = currentList.filter { it.kindOf == categories }
            _filterList.postValue(newList.toMutableList())
        }

    }
    fun filterStartsWithNameCaseInsensitive(name: String){
        val currentList = _downloadList.value
        if(name == ""){
            _filterList.postValue(currentList)
            return
        }
        if(currentList != null){
            val newList = currentList.filter { it.fileName.lowercase().startsWith(name.lowercase()) }
            _filterList.postValue(newList.toMutableList())

        }
    }

}