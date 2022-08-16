package com.quangln2.downloadmanagerrefactor.domain.remote

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import kotlinx.coroutines.flow.Flow

class DownloadAFileUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile, context: Context): Flow<StructureDownFile> =
        downloadRepository.downloadAFile(file, context)

}