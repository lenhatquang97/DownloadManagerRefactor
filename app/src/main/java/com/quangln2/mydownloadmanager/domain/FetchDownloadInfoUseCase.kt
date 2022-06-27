package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class FetchDownloadInfoUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(file: StrucDownFile) : StrucDownFile = downloadRepository.fetchDownloadInfo(file)

}