package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class DownloadAFileUseCase(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(file: StrucDownFile, context: Context) = downloadRepository.downloadAFile(file, context)

}