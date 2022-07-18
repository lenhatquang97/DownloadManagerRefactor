package com.quangln2.mydownloadmanager.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.model.StrucDownFile

object DownloadManagerController {
    var downloadListSchema: LiveData<List<StrucDownFile>>? = null

    var _inputItem =
        MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val inputItem: LiveData<StrucDownFile> get() = _inputItem

    var _fetchedFileInfo = MutableLiveData<StrucDownFile>()
    val fetchedFileInfo: LiveData<StrucDownFile> get() = _fetchedFileInfo

    var _downloadList =
        MutableLiveData<MutableList<StrucDownFile>>().apply { value = mutableListOf() }
    val downloadList: LiveData<MutableList<StrucDownFile>> get() = _downloadList

    var _progressFile =
        MutableLiveData<StrucDownFile>().apply { value = ServiceLocator.initializeStrucDownFile() }
    val progressFile: LiveData<StrucDownFile> get() = _progressFile

    const val MAX_DOWNLOAD_THREAD = 2
    var howManyFileDownloadingParallel = 0

}