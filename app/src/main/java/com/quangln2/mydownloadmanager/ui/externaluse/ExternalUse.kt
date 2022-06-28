package com.quangln2.mydownloadmanager.ui.externaluse

import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.domain.*

class ExternalUse {
    companion object{
        val getRepo = DownloadManagerApplication().downloadRepository
        val writeToFileAPI29AboveUseCase = WriteToFileAPI29AboveUseCase(getRepo)
        val writeToFileAPI29BelowUseCase = WriteToFileAPI29BelowUseCase(getRepo)
        val pauseDownloadUseCase = PauseDownloadUseCase(getRepo)
        val resumeDownloadUseCase = ResumeDownloadUseCase(getRepo)
        val retryDownloadUseCase = RetryDownloadUseCase(getRepo)
        val downloadAFileUseCase = DownloadAFileUseCase(getRepo)
        val insertToListUseCase = InsertToListUseCase(getRepo)
    }
}