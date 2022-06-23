package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class HomeViewModel(
    private val addNewDownloadInfoUseCase: AddNewDownloadInfoUseCase,
    private val downloadAFileUseCase: DownloadAFileUseCase,
    private val fetchDownloadInfoUseCase: FetchDownloadInfoUseCase,
    private val pauseDownloadUseCase: PauseDownloadUseCase,
    private val resumeDownloadUseCase: ResumeDownloadUseCase,
    private val context: Context
): ViewModel() {
    var _item = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val item: LiveData<StrucDownFile> get() = _item

    var _downloadList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

    fun addNewDownloadInfo(url: String, downloadTo: String){
        addNewDownloadInfoUseCase(url, downloadTo, _item.value!!)
    }

    fun fetchDownloadInfoToUI(){
        fetchDownloadInfoUseCase(_item.value!!)
    }


    fun downloadAFile(){
        val currentList = _downloadList.value
        currentList?.add(_item.value!!)
        _downloadList.postValue(currentList)

//        downloadAFileUseCase(_item.value!!, context)
//        withContext(Dispatchers.Main.immediate){
//            println("In another scope")
//        }
    }
    fun changeFile(){
        _item.value?.kindOf = "Video"
        val newFile = _item.value
        _item.postValue(newFile)
    }
}