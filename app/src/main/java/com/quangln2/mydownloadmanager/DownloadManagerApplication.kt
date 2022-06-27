package com.quangln2.mydownloadmanager

import android.app.Application
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.domain.DownloadAFileUseCase
import com.quangln2.mydownloadmanager.domain.PauseDownloadUseCase
import com.quangln2.mydownloadmanager.domain.ResumeDownloadUseCase
import com.quangln2.mydownloadmanager.domain.RetryDownloadUseCase

class DownloadManagerApplication: Application() {
    val downloadRepository: DownloadRepository get() = ServiceLocator.provideDownloadRepository()
    companion object{
        private val defaultDownloadRepository = DefaultDownloadRepository()
        val downloadAFileUseCase = DownloadAFileUseCase(defaultDownloadRepository)
        val pauseDownloadUseCase = PauseDownloadUseCase(defaultDownloadRepository)
        val resumeDownloadUseCase = ResumeDownloadUseCase(defaultDownloadRepository)
        val retryDownloadUseCase = RetryDownloadUseCase(defaultDownloadRepository)
    }
}