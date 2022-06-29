package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class InsertToListUseCase(private val repository: DownloadRepository) {
    suspend operator fun invoke(download: StrucDownFile) = repository.insert(download)
}