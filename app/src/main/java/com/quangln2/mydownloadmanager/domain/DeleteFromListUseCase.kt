package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class DeleteFromListUseCase(private val downloadRepository: DefaultDownloadRepository) {
    suspend operator fun invoke(file: StrucDownFile) = downloadRepository.deleteFromList(file)
}