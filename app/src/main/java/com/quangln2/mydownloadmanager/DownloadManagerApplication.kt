package com.quangln2.mydownloadmanager

import android.app.Application
import com.quangln2.mydownloadmanager.ServiceLocator.downloadRepository
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.domain.DownloadAFileUseCase
import com.quangln2.mydownloadmanager.domain.PauseDownloadUseCase
import com.quangln2.mydownloadmanager.domain.ResumeDownloadUseCase

class DownloadManagerApplication: Application() {
    val downloadRepository: DownloadRepository get() = ServiceLocator.provideDownloadRepository()
    companion object{
        val downloadAFileUseCase = DownloadAFileUseCase(DefaultDownloadRepository())
        val pauseDownloadUseCase = PauseDownloadUseCase(DefaultDownloadRepository())
        val resumeDownloadUseCase = ResumeDownloadUseCase(DefaultDownloadRepository())
    }
}