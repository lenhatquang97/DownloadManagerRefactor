package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class UpdateToListUseCase(private val repository: DownloadRepository) {
    suspend operator fun invoke(download: StrucDownFile) = repository.update(download)
}