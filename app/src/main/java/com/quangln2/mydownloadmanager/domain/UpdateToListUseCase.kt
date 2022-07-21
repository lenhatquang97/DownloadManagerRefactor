package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class UpdateToListUseCase(private val repository: DefaultDownloadRepository) {
    suspend operator fun invoke(download: StructureDownFile) = repository.update(download)
}