package com.quangln2.downloadmanagerrefactor.domain.remote

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class StopDownloadUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile, context: Context) =
        downloadRepository.stopDownload(file, context)
}