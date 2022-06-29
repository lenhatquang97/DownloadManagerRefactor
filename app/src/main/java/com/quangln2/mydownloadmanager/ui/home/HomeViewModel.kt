package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.domain.*
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.*

class HomeViewModel(
    private val addNewDownloadInfoUseCase: AddNewDownloadInfoUseCase,
    private val fetchDownloadInfoUseCase: FetchDownloadInfoUseCase,
    private val context: Context
): ViewModel() {
    var _item = MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val item: LiveData<StrucDownFile> get() = _item

    //val database by lazy{ DownloadDatabase.getDatabase(context)}
    //val downloadRepository by lazy{ServiceLocator.provideDownloadRepository(database.downloadDao())}

    val downloadList: LiveData<List<StrucDownFile>> get() = DownloadManagerApplication().database.downloadDao().getAll().asLiveData()

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

    fun downloadAFile() = viewModelScope.launch{
        if(_item.value != null){
            _item.value!!.downloadState = DownloadStatusState.DOWNLOADING
            val currentVal = _item.value!!.copy()
            CoroutineScope(Dispatchers.IO).launch {
                ExternalUse.insertToListUseCase(context)(currentVal)
            }
        }

    }
}