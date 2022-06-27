package com.quangln2.mydownloadmanager

import android.app.Application
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.domain.*

class DownloadManagerApplication: Application() {
    val downloadRepository: DownloadRepository get() = ServiceLocator.provideDownloadRepository()
    companion object{
        private val defaultDownloadRepository = DefaultDownloadRepository()
        val downloadAFileUseCase = DownloadAFileUseCase(defaultDownloadRepository)
        val writeToFileAPI29AboveUseCase = WriteToFileAPI29AboveUseCase(defaultDownloadRepository)
        val writeToFileAPI29BelowUseCase = WriteToFileAPI29BelowUseCase(defaultDownloadRepository)
        val pauseDownloadUseCase = PauseDownloadUseCase(defaultDownloadRepository)
        val resumeDownloadUseCase = ResumeDownloadUseCase(defaultDownloadRepository)
        val retryDownloadUseCase = RetryDownloadUseCase(defaultDownloadRepository)
    }
}