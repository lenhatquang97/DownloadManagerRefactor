package com.quangln2.downloadmanagerrefactor.domain.remote

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class AddNewDownloadInfoUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(url: String, downloadTo: String, file: StructureDownFile) {
        downloadRepository.addNewDownloadInfo(url, downloadTo, file)
    }
}