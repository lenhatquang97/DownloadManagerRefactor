package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class RetryDownloadUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile, context: Context) =
        downloadRepository.retryDownload(file, context)
}