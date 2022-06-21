package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class CreateConnectionUseCase(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(file: StrucDownFile) = downloadRepository.createConnection(file)
}