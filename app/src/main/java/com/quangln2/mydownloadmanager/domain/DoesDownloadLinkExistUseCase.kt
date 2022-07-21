package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class DoesDownloadLinkExistUseCase(private val downloadRepository: DefaultDownloadRepository) {
    suspend operator fun invoke(file: StructureDownFile): Boolean =
        downloadRepository.doesDownloadLinkExist(file)
}