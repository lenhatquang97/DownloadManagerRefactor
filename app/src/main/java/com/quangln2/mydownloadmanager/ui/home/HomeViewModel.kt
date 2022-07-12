package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    val deletePermanentlyUseCase: DeletePermanentlyUseCase,
    val deleteFromListUseCase: DeleteFromListUseCase,
    val addNewDownloadInfo: AddNewDownloadInfoUseCase,
    val fetchDownloadInfo: FetchDownloadInfoUseCase,
    val writeToFileAPI29AboveUseCase: WriteToFileAPI29AboveUseCase,
    val writeToFileAPI29BelowUseCase: WriteToFileAPI29BelowUseCase,
    val insertToListUseCase: InsertToListUseCase,
    val downloadAFileUseCase: DownloadAFileUseCase,
    val retryDownloadUseCase: RetryDownloadUseCase,
    val isFileExistingUseCase: IsFileExistingUseCase,
    val getBytesFromExistingFileUseCase: GetBytesFromExistingFileUseCase,
    val openDownloadFileUseCase: OpenDownloadFileUseCase,
    val updateToListUseCase: UpdateToListUseCase


) : ViewModel() {
    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }
    var _filterList = MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StrucDownFile>> get() = _filterList

    fun getDataFromDatabase(){
        DownloadManagerController.downloadListSchema?.value?.let{
            DownloadManagerController._downloadList.value?.let { ls ->
                if(ls.size != 0){
                    DownloadManagerController._downloadList.value = it.toMutableList()
                    _filterList.value = it.toMutableList()
                }
            }
        }
    }

    fun filterList(downloadStatusState: String){
        if(downloadStatusState == DownloadStatusState.ALL.toString()){
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(DownloadManagerController.downloadList.value?.toMutableList())
            }
            return
        }
        val currentList = DownloadManagerController._downloadList.value
        if(currentList != null){
            CoroutineScope(Dispatchers.IO).launch {
                val newList = currentList.filter { it.downloadState.toString() == downloadStatusState }
                _filterList.postValue(newList.toMutableList())
            }

        }
    }

    fun filterCategories(categories: String){
        val currentList = DownloadManagerController._downloadList.value
        if(categories == "All"){
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(currentList?.toMutableList())
            }
            return
        }
        if(currentList != null){
            CoroutineScope(Dispatchers.IO).launch {
                val newList = currentList.filter { it.kindOf == categories }
                _filterList.postValue(newList.toMutableList())
            }

        }

    }
    fun filterStartsWithNameCaseInsensitive(name: String){
        val currentList = DownloadManagerController._downloadList.value
        if(name == ""){
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(currentList?.toMutableList())
            }
            return
        }
        if(currentList != null){
            CoroutineScope(Dispatchers.IO).launch {
                val newList = currentList.filter { it.fileName.lowercase().startsWith(name.lowercase()) }
                _filterList.postValue(newList.toMutableList())
            }
        }
    }
    fun deleteFromList(context: Context, file: StrucDownFile){
        CoroutineScope(Dispatchers.IO).launch {

            deleteFromListUseCase(file)
            val res = DownloadManagerController.downloadList.value?.filter { it.id != file.id }?.toMutableList()
            DownloadManagerController._downloadList.postValue(res)
            _filterList.postValue(res)
        }
    }
    fun deletePermanently(context: Context, file: StrucDownFile, onHandle: (Boolean)-> Unit){
        val builder =
            MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                .setTitle(file.fileName)
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setMessage("Are you sure that you will delete this file? You cannot undo this action.")
                .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        deletePermanentlyUseCase(file, context)
                        val res = DownloadManagerController.downloadList.value?.filter { it.id != file.id }?.toMutableList()
                        DownloadManagerController._downloadList.postValue(res)
                        _filterList.postValue(res)
                        withContext(Dispatchers.Main){
                            onHandle(true)
                        }
                        a.dismiss()

                    }
                }
                .setNegativeButton(ConstantClass.NEGATIVE_BUTTON) { a, _ ->
                    onHandle(false)
                    a.dismiss()
                }
        builder.show()
    }


    fun addNewDownloadInfo(context: Context, url: String, downloadTo: String){
        if(DownloadManagerController._inputItem.value != null){
            addNewDownloadInfo(url, downloadTo, DownloadManagerController._inputItem.value!!)
        }
    }

    fun fetchDownloadFileInfo(context: Context){
        val file = DownloadManagerController.inputItem.value
        CoroutineScope(Dispatchers.IO).launch{
            if(file != null){
                fetchDownloadInfo(file)
                DownloadManagerController._fetchedFileInfo.postValue(file)
            }
        }
    }

    fun downloadAFile(context: Context) {
        val file = DownloadManagerController._fetchedFileInfo.value
        if(file != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                writeToFileAPI29AboveUseCase(file, context)
            } else {
                writeToFileAPI29BelowUseCase(file)
            }

            val currentList = DownloadManagerController.downloadList.value
            if(currentList != null){
                currentList.add(file.copy(downloadState = DownloadStatusState.DOWNLOADING))
                DownloadManagerController._downloadList.postValue(currentList)

            }
            CoroutineScope(Dispatchers.IO).launch{
                insertToListUseCase(file.copy(downloadState = DownloadStatusState.DOWNLOADING))
            }
            val addedFile = currentList?.last()
            if (addedFile != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    downloadAFileUseCase(addedFile, context).collect {
                        DownloadManagerController._progressFile.value = it
                    }
                }
            }
        }
    }
    fun pause(id: String){
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.PAUSED
            DownloadManagerController._progressFile.value = currentFile
        }
    }
    fun resume(context: Context, id: String){
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if(index != null && index != -1){
            val currentFile = currentList[index]
            val doesFileExist = isFileExistingUseCase(currentFile, context)
            if(doesFileExist){
                currentFile.bytesCopied = getBytesFromExistingFileUseCase(currentFile, context)
                currentFile.downloadState = DownloadStatusState.DOWNLOADING
                CoroutineScope(Dispatchers.Main).launch {
                    downloadAFileUseCase(currentFile, context).collect {
                        DownloadManagerController._progressFile.value = it
                    }
                }
            } else {
                currentFile.downloadState = DownloadStatusState.FAILED
                DownloadManagerController._progressFile.value = currentFile
            }
        }
    }

    fun retry(context: Context, item: StrucDownFile){
        retryDownloadUseCase(item, context)
        CoroutineScope(Dispatchers.Main).launch {
            downloadAFileUseCase(item, context).collect {
                DownloadManagerController._progressFile.value = it
            }
        }
    }
    fun open(context: Context, item: StrucDownFile){
        val doesFileExist = isFileExistingUseCase(item, context)
        if(doesFileExist){
            openDownloadFileUseCase(item,context)
        }
    }
    fun update(item: StrucDownFile){
        CoroutineScope(Dispatchers.IO).launch {
            updateToListUseCase(item)
        }
    }


}