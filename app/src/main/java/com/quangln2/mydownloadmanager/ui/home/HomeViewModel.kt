package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.quangln2.mydownloadmanager.listener.OnAcceptPress
import com.quangln2.mydownloadmanager.service.DownloadService
import com.quangln2.mydownloadmanager.util.DownloadUtil
import com.quangln2.mydownloadmanager.util.UIComponentUtil
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
    val doesDownloadLinkExistUseCase: DoesDownloadLinkExistUseCase
) : ViewModel() {
    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }
    var _filterList =
        MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val filterList: LiveData<MutableList<StrucDownFile>> get() = _filterList


    var textSearch = MutableLiveData<String>().apply { value = "" }


    fun preProcessingDownloadFile(context: Context, file: StrucDownFile){
        val onAcceptPress = object : OnAcceptPress{
            override fun onAcceptPress() {
                val intent = Intent(context, DownloadService::class.java)
                intent.putExtra("command", "WaitForDownload")
                intent.putExtra("item", file)
                context.startService(intent)
            }
        }

        //Does same download link exist?
        CoroutineScope(Dispatchers.IO).launch {
            val result = doesDownloadLinkExistUseCase(file)
            if (result){
                withContext(Dispatchers.Main){
                    UIComponentUtil.showDownloadDialogAgain(context, file, onAcceptPress)
                }
            } else {
                withContext(Dispatchers.Main){
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

    fun deleteFromList(file: StrucDownFile) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteFromListUseCase(file)
            val res = DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                ?.toMutableList()
            DownloadManagerController._downloadList.postValue(res)
            _filterList.postValue(res)
        }
    }

    fun deletePermanently(context: Context, file: StrucDownFile, onHandle: (Boolean) -> Unit) {
        val builder =
            MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                .setTitle(file.fileName)
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setMessage("Are you sure that you will delete this file? You cannot undo this action.")
                .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        deletePermanentlyUseCase(file, context)
                        val res =
                            DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                                ?.toMutableList()
                        DownloadManagerController._downloadList.postValue(res)
                        _filterList.postValue(res)
                        withContext(Dispatchers.Main) {
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

    fun pause(id: String) {
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if (index != null && index != -1) {
            val currentFile = currentList[index]
            currentFile.downloadState = DownloadStatusState.PAUSED
            DownloadManagerController._progressFile.value = currentFile
        }
    }

    fun resume(context: Context, id: String) {
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == id }
        if (index != null && index != -1) {
            val currentFile = currentList[index]
            val doesFileExist = DownloadUtil.isFileExisting(currentFile, context)
            if (doesFileExist) {
                currentFile.bytesCopied =
                    DownloadUtil.getBytesFromExistingFile(currentFile, context)
                currentFile.downloadState = DownloadStatusState.DOWNLOADING
            } else {
                currentFile.downloadState = DownloadStatusState.FAILED
                DownloadManagerController._progressFile.value = currentFile
            }
            sendToDownloadService(context, currentFile)
        }
    }

    fun retry(context: Context, item: StrucDownFile) {
        retryDownloadUseCase(item, context)
        sendToDownloadService(context, item)
    }

    fun open(context: Context, item: StrucDownFile) {
        val doesFileExist = DownloadUtil.isFileExisting(item, context)
        if (doesFileExist) {
            openDownloadFileUseCase(item, context)
        }
    }

    fun update(item: StrucDownFile) {
        CoroutineScope(Dispatchers.IO).launch {
            updateToListUseCase(item)
        }
    }

    fun vibratePhone(context: Context) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vib.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }

    }

    private fun sendToDownloadService(context: Context, currentFile: StrucDownFile) {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra("item", currentFile)
        context.startService(intent)
    }

}