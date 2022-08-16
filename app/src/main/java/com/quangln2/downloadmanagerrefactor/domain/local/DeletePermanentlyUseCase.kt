package com.quangln2.downloadmanagerrefactor.domain.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class DeletePermanentlyUseCase(private val downloadRepository: DefaultDownloadRepository) {
    suspend operator fun invoke(file: StructureDownFile, context: Context) =
        downloadRepository.deletePermanently(file, context)
}