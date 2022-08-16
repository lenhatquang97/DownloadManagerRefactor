package com.quangln2.downloadmanagerrefactor.domain.local

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class InsertToListUseCase(private val repository: DefaultDownloadRepository) {
    suspend operator fun invoke(download: StructureDownFile) = repository.insert(download)
}