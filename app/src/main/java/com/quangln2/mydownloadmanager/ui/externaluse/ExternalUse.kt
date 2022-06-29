package com.quangln2.mydownloadmanager.ui.externaluse

import android.content.Context
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.domain.*

class ExternalUse() {
    companion object{
        fun database(context: Context): DownloadDatabase{
            return DownloadDatabase.getDatabase(context)
        }
        fun getRepo(context: Context): DownloadRepository = ServiceLocator.provideDownloadRepository(database(context).downloadDao())

        fun writeToFileAPI29AboveUseCase(context: Context): WriteToFileAPI29AboveUseCase{
            return WriteToFileAPI29AboveUseCase(getRepo(context))
        }
        fun writeToFileAPI29BelowUseCase(context: Context): WriteToFileAPI29BelowUseCase{
            return WriteToFileAPI29BelowUseCase(getRepo(context))
        }
        fun pauseDownloadUseCase(context: Context): PauseDownloadUseCase{
            return PauseDownloadUseCase(getRepo(context))
        }
        fun resumeDownloadUseCase(context: Context): ResumeDownloadUseCase{
            return ResumeDownloadUseCase(getRepo(context))
        }
        fun retryDownloadUseCase(context: Context): RetryDownloadUseCase{
            return RetryDownloadUseCase(getRepo(context))
        }
        fun downloadAFileUseCase(context: Context): DownloadAFileUseCase{
            return DownloadAFileUseCase(getRepo(context))
        }
        fun insertToListUseCase(context: Context): InsertToListUseCase{
            return InsertToListUseCase(getRepo(context))
        }
    }
}