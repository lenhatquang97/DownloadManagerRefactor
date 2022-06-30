package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse

class StopDownloadUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(file: StrucDownFile){
        downloadRepository.stopDownload(file)
    }
}