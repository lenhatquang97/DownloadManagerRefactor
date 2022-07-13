package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class UpdateToListUseCase(private val repository: DefaultDownloadRepository) {
    suspend operator fun invoke(download: StrucDownFile) = repository.update(download)
}