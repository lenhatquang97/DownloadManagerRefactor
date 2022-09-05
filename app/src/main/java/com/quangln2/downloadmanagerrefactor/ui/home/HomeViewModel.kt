package com.quangln2.downloadmanagerrefactor.ui.home

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController._filterList
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController._inputItem
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.data.source.protocol.HttpProtocol
import com.quangln2.downloadmanagerrefactor.data.source.protocol.SocketProtocol
import com.quangln2.downloadmanagerrefactor.domain.local.*
import com.quangln2.downloadmanagerrefactor.domain.remote.*
import com.quangln2.downloadmanagerrefactor.listener.OnAcceptPress
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
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
    var textSearch = MutableLiveData<String>().apply { value = "" }

    fun preProcessingDownloadFile(context: Context, file: StructureDownFile) {


        val onAcceptPress = object : OnAcceptPress {
            override fun onAcceptPress() {
                val intent = Intent(context, DownloadService::class.java)
                intent.putExtra("command", "WaitForDownload")
                DownloadManagerController.newItem.value = file
                context.startService(intent)
            }

            override fun onNegativePress() {
                if (file.protocol == "Socket") {
                    (file.protocolInterface as SocketProtocol).closeConnection()
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
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
        val currentList = DownloadManagerController._downloadList.value
        if (currentList != null) {
            viewModelScope.launch(Dispatchers.IO) {
                if (downloadStatusState == DownloadStatusState.ALL.toString()) {
                    _filterList.postValue(currentList.toMutableList())
                } else {
                    val newList =
                        currentList.filter { it.downloadState.toString() == downloadStatusState }
                    _filterList.postValue(newList.toMutableList())
                }

            }

        }
    }


    fun filterStartsWithNameCaseInsensitive(name: String) {
        val currentList = DownloadManagerController._downloadList.value
        if (name == "") {
            viewModelScope.launch(Dispatchers.IO) {
                _filterList.postValue(currentList?.toMutableList())
            }
            return
        }
        if (currentList != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val newList =
                    currentList.filter { it.fileName.lowercase().startsWith(name.lowercase()) }
                _filterList.postValue(newList.toMutableList())
            }
        }
    }

    fun deleteFromList(file: StructureDownFile, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteFromListUseCase(file, context)
            val res = DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                ?.toMutableList()

            withContext(Dispatchers.Main) {
                DownloadManagerController._downloadList.value = res
                _filterList.value = res
            }
        }
    }

    fun deletePermanently(context: Context, file: StructureDownFile, onHandle: (Boolean) -> Unit) {
        val builder =
            MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                .setTitle(file.fileName)
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setMessage("Are you sure that you will delete this file? You cannot undo this action.")
                .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                    viewModelScope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.Main) {
                            onHandle(true)
                        }
                        deletePermanentlyUseCase(file, context)

                        //Double removal
                        withContext(Dispatchers.Main) {
                            val res =
                                DownloadManagerController.downloadList.value?.filter { it.id != file.id }
                                    ?.toMutableList()
                            DownloadManagerController._downloadList.value = res
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

    fun addNewDownloadInfo(url: String, downloadTo: String): Boolean {
        try {
            val item = StructureDownFile()
            val isValidIpPort = ConstantClass.CHECK_IP_PORT_PATH.toRegex().matches(url)
            val isValidURL = URLUtil.isValidUrl(url)
            if (isValidIpPort) {
                val ip = url.split(":")[0]
                val port = url.split(":")[1].split("/")[0].toInt()
                item.protocol = "Socket"
                try {
                    item.protocolInterface = SocketProtocol(ip, port)
                } catch (e: Exception) {
                    return false
                }
                addNewDownloadInfo(url, downloadTo, item)
                _inputItem.value = item.copy()
            } else if (isValidURL) {
                item.protocol = "HTTP"
                item.protocolInterface = HttpProtocol()
                addNewDownloadInfo(url, downloadTo, item)
                _inputItem.value = item.copy()
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun fetchDownloadFileInfo(onHandle: (StructureDownFile) -> Unit) {
        val file = DownloadManagerController.inputItem.value
        if (file != null) {
            val res = fetchDownloadInfo(file)
            onHandle(res)
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
        viewModelScope.launch(Dispatchers.IO) {
            updateToListUseCase(item)
        }
    }

    fun stop(item: StructureDownFile, context: Context) = stopDownloadUseCase(item, context)
    fun vibratePhone(context: Context) = vibratePhoneUseCase(context)


    private fun sendToDownloadService(context: Context, currentFile: StructureDownFile) {
        val intent = Intent(context, DownloadService::class.java)
        DownloadManagerController.newItem.value = currentFile
        context.startService(intent)
    }

}