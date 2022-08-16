package com.quangln2.downloadmanagerrefactor.domain.local

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class DoesDownloadLinkExistUseCase(private val downloadRepository: DefaultDownloadRepository) {
    suspend operator fun invoke(file: StructureDownFile): Boolean =
        downloadRepository.doesDownloadLinkExist(file)
}