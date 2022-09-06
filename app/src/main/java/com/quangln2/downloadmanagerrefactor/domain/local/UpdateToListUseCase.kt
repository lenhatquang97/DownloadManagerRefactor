package com.quangln2.downloadmanagerrefactor.domain.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class UpdateToListUseCase(private val repository: DefaultDownloadRepository) {
    suspend operator fun invoke(download: StructureDownFile, context: Context) = repository.update(download, context)
}