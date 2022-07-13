package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class AddNewDownloadInfoUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(url: String, downloadTo: String, file: StrucDownFile){
        downloadRepository.addNewDownloadInfo(url, downloadTo, file)
    }
}