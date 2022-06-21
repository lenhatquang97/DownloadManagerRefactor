package com.quangln2.mydownloadmanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.domain.*
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val downloadRepository: DownloadRepository,
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel(
                        AddNewDownloadInfoUseCase(downloadRepository),
                        DownloadAFileUseCase(downloadRepository),
                        FetchDownloadInfoUseCase(downloadRepository),
                        PauseDownloadUseCase(downloadRepository),
                        ResumeDownloadUseCase(downloadRepository),
                        context
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
