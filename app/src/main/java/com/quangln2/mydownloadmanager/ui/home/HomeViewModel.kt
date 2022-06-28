package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.domain.*
import kotlinx.coroutines.*

class HomeViewModel(
    private val addNewDownloadInfoUseCase: AddNewDownloadInfoUseCase,
    private val fetchDownloadInfoUseCase: FetchDownloadInfoUseCase,
    private val pauseDownloadUseCase: PauseDownloadUseCase,
    private val resumeDownloadUseCase: ResumeDownloadUseCase,
    private val context: Context
): ViewModel() {
    var _item = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val item: LiveData<StrucDownFile> get() = _item

    var _downloadList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

    private var _fetchedFileInfo = MutableLiveData<StrucDownFile>()
    val fetchedFileInfo : LiveData<StrucDownFile> get() = _fetchedFileInfo



    fun addNewDownloadInfo(url: String, downloadTo: String){
        addNewDownloadInfoUseCase(url, downloadTo, _item.value!!)
    }

    fun fetchDownloadFileInfo(){
        val file = item.value
        viewModelScope.launch(Dispatchers.IO){
            if(file != null){
                fetchDownloadInfoUseCase(file)
                _fetchedFileInfo.postValue(file)
            }
        }
    }

    fun downloadAFile(){
        if(_item.value != null){
            _item.value!!.downloadState = DownloadStatusState.DOWNLOADING
            val currentList = _downloadList.value
            currentList?.add(_item.value!!.copy())
            _downloadList.postValue(currentList)
            _fetchedFileInfo.postValue(null)
        }

    }
}