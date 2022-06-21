package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class AddNewDownloadInfoUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(url: String, downloadTo: String, file: StrucDownFile){
        downloadRepository.addNewDownloadInfo(url, downloadTo, file)
    }
}