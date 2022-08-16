package com.quangln2.downloadmanagerrefactor.ui.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.domain.local.*
import com.quangln2.downloadmanagerrefactor.domain.remote.*
import com.quangln2.downloadmanagerrefactor.listener.OnAcceptPress
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    val deletePermanentlyUseCase: DeletePermanentlyUseCase,
    val deleteFromListUseCase: DeleteFromListUseCase,
    val addNewDownloadInfo: AddNewDownloadInfoUseCase,
    val fetchDownloadInfo: FetchDownloadInfoUseCase,
    val retryDownloadUseCase: RetryDownloadUseCase,
    val openDownloadFileUseCase: OpenDownloadFileUseCase,
    val updateToListUseCase: UpdateToListUseCase,
    val doesDownloadLinkExistUseCase: DoesDownloadLinkExistUseCase,
    val resumeDownloadUseCase: ResumeDownloadUseCase,
    val pauseDownloadUseCase: PauseDownloadUseCase,
    val stopDownloadUseCase: StopDownloadUseCase,
    val vibratePhoneUseCase: VibratePhoneUseCase
) : ViewModel() {
    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }
    var _filterList =
        MutableLiveData<MutableList<StructureDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StructureDownFile>> get() = _filterList
    var textSearch = MutableLiveData<String>().apply { value = "" }

    fun preProcessingDownloadFile(context: Context, file: StructureDownFile) {
        val onAcceptPress = object : OnAcceptPress {
            override fun onAcceptPress() {
                val intent = Intent(context, DownloadService::class.java)
                intent.putExtra("command", "WaitForDownload")
                intent.putExtra("item", file)
                context.startService(intent)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val result = doesDownloadLinkExistUseCase(file)
            if (result) {
                withContext(Dispatchers.Main) {
                    UIComponentUtil.showDownloadDialogAgain(context, file, onAcceptPress)
                }
            } else {
                withContext(Dispatchers.Main) {
                    UIComponentUtil.showDownloadAlertDialog(context, file, onAcceptPress)
                }
            }
        }
    }

    fun filterList(downloadStatusState: String) {
        if (downloadStatusState == DownloadStatusState.ALL.toString()) {
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(DownloadManagerController.downloadList.value?.toMutableList())
            }
            return
        }
        val currentList = DownloadManagerController._downloadList.value
        if (currentList != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val newList =
                    currentList.filter { it.downloadState.toString() == downloadStatusState }
                _filterList.postValue(newList.toMutableList())
            }

        }
    }

    fun filterCategories(categories: String) {
        val currentList = DownloadManagerController.downloadList.value
        if (categories == "All") {
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(currentList?.toMutableList())
            }
            return
        }
        if (currentList != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val newList = currentList.filter { it.kindOf == categories }
                _filterList.postValue(newList.toMutableList())
            }

        }

    }

    fun filterStartsWithNameCaseInsensitive(name: String) {
        val currentList = DownloadManagerController._downloadList.value
        if (name == "") {
            CoroutineScope(Dispatchers.IO).launch {
                _filterList.postValue(currentList?.toMutableList())
            }
            return
        }
        if (currentList != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val newList =
                    currentList.filter { it.fileName.lowercase().startsWith(name.lowercase()) }
                _filterList.postValue(newList.toMutableList())
            }
        }
    }

    fun deleteFromList(file: StructureDownFile, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteFromListUseCase(file, context)
            val res = DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                ?.toMutableList()
            DownloadManagerController._downloadList.postValue(res)
            _filterList.postValue(res)
        }
    }

    fun deletePermanently(context: Context, file: StructureDownFile, onHandle: (Boolean) -> Unit) {
        val builder =
            MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                .setTitle(file.fileName)
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setMessage("Are you sure that you will delete this file? You cannot undo this action.")
                .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            onHandle(true)
                        }
                        deletePermanentlyUseCase(file, context)
                        val res =
                            DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                                ?.toMutableList()
                        DownloadManagerController._downloadList.postValue(res)
                        _filterList.postValue(res)
                        a.dismiss()

                    }
                }
                .setNegativeButton(ConstantClass.NEGATIVE_BUTTON) { a, _ ->
                    onHandle(false)
                    a.dismiss()
                }
        builder.show()
    }

    fun addNewDownloadInfo(url: String, downloadTo: String) {
        if (DownloadManagerController._inputItem.value != null) {
            addNewDownloadInfo(url, downloadTo, DownloadManagerController._inputItem.value!!)
        }
    }

    fun fetchDownloadFileInfo() {
        val file = DownloadManagerController.inputItem.value
        CoroutineScope(Dispatchers.IO).launch {
            if (file != null) {
                fetchDownloadInfo(file)
                DownloadManagerController._fetchedFileInfo.postValue(file)
            }
        }
    }

    fun pause(item: StructureDownFile) = pauseDownloadUseCase(item)

    fun resume(item: StructureDownFile, context: Context) {
        resumeDownloadUseCase(item, context)
        sendToDownloadService(context, item)
    }

    fun retry(context: Context, item: StructureDownFile) {
        retryDownloadUseCase(item, context)
        sendToDownloadService(context, item)
    }

    fun open(context: Context, item: StructureDownFile) = openDownloadFileUseCase(item, context)

    fun update(item: StructureDownFile) {
        val currentList = DownloadManagerController._downloadList.value
        DownloadManagerController._downloadList.postValue(currentList?.map { if (it.id == item.id) item else it }
            ?.toMutableList())

        CoroutineScope(Dispatchers.IO).launch {
            updateToListUseCase(item)
        }
    }

    fun stop(item: StructureDownFile) = stopDownloadUseCase(item)
    fun vibratePhone(context: Context) = vibratePhoneUseCase(context)


    private fun sendToDownloadService(context: Context, currentFile: StructureDownFile) {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra("item", currentFile)
        context.startService(intent)
    }

}