package com.quangln2.downloadmanagerrefactor.domain.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class InsertToListUseCase(private val repository: DefaultDownloadRepository) {
    suspend operator fun invoke(download: StructureDownFile, context: Context) = repository.insert(download, context)
}