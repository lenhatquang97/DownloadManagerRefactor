package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class QueueDownloadUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StrucDownFile) = downloadRepository.queueDownload(file)

}