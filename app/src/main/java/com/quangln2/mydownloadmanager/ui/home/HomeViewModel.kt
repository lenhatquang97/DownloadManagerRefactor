package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
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
            ExternalUse.deleteFromListUseCase(context).invoke(file)
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
                        ExternalUse.deletePermanentlyUseCase(context).invoke(file, context)
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

}