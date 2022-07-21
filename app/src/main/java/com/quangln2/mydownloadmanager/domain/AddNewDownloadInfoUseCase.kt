package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class AddNewDownloadInfoUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(url: String, downloadTo: String, file: StructureDownFile) {
        downloadRepository.addNewDownloadInfo(url, downloadTo, file)
    }
}