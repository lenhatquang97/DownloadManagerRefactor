package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class FetchDownloadInfoUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile): StructureDownFile =
        downloadRepository.fetchDownloadInfo(file)

}