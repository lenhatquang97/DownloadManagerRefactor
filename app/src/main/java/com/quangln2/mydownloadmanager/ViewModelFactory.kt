package com.quangln2.mydownloadmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.domain.*
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val downloadRepository: DefaultDownloadRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel(
                        DeletePermanentlyUseCase(downloadRepository),
                        DeleteFromListUseCase(downloadRepository),
                        AddNewDownloadInfoUseCase(downloadRepository),
                        FetchDownloadInfoUseCase(downloadRepository),
                        WriteToFileAPI29AboveUseCase(downloadRepository),
                        WriteToFileAPI29BelowUseCase(downloadRepository),
                        InsertToListUseCase(downloadRepository),
                        DownloadAFileUseCase(downloadRepository),
                        RetryDownloadUseCase(downloadRepository),
                        OpenDownloadFileUseCase(downloadRepository),
                        UpdateToListUseCase(downloadRepository)
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
