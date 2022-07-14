package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class StopDownloadUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StrucDownFile) {
        downloadRepository.stopDownload(file)
    }
}