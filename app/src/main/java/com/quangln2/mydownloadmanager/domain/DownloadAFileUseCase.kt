package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class DownloadAFileUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(file: StrucDownFile, context: Context): Flow<StrucDownFile> = downloadRepository.downloadAFile(file, context)

}