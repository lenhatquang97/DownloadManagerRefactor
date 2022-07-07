package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Build
import androidx.lifecycle.*
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.downloadList
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.domain.*
import com.quangln2.mydownloadmanager.listener.ProgressCallback
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*

class HomeViewModel : ViewModel() {
    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }
}