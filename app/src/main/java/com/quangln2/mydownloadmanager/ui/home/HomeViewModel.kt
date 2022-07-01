package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Build
import androidx.lifecycle.*
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.domain.*
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.*
import java.util.*

class HomeViewModel(
    private val addNewDownloadInfoUseCase: AddNewDownloadInfoUseCase,
    private val fetchDownloadInfoUseCase: FetchDownloadInfoUseCase,
    private val context: Context
): ViewModel() {

    var _item = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val item: LiveData<StrucDownFile> get() = _item

    var _downloadList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

   var downloadListSchema: LiveData<List<StrucDownFile>> =
        (DownloadManagerApplication()).database.downloadDao().getAll().asLiveData()

    var _filterList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StrucDownFile>> get() = _filterList

    private var _fetchedFileInfo = MutableLiveData<StrucDownFile>()
    val fetchedFileInfo : LiveData<StrucDownFile> get() = _fetchedFileInfo

    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }


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

    fun downloadAFile() {
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
        }

    }

    fun filterList(downloadStatusState: String){
        val currentList = _downloadList.value
        if(currentList != null){
            val newList = currentList.filter { it.downloadState.toString() == downloadStatusState }
            _filterList.postValue(newList.toMutableList())
        }
    }
}