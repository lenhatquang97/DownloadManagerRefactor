package com.quangln2.downloadmanagerrefactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import com.quangln2.downloadmanagerrefactor.domain.local.*
import com.quangln2.downloadmanagerrefactor.domain.remote.*
import com.quangln2.downloadmanagerrefactor.ui.home.HomeViewModel

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
                        RetryDownloadUseCase(downloadRepository),
                        OpenDownloadFileUseCase(downloadRepository),
                        UpdateToListUseCase(downloadRepository),
                        DoesDownloadLinkExistUseCase(downloadRepository),
                        ResumeDownloadUseCase(downloadRepository),
                        PauseDownloadUseCase(downloadRepository),
                        StopDownloadUseCase(downloadRepository),
                        VibratePhoneUseCase(downloadRepository)
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
