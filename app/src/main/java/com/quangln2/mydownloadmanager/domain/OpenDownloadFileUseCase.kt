package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.ServiceLocator.downloadRepository
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class OpenDownloadFileUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(file: StrucDownFile, context: Context) = downloadRepository.openDownloadFile(file, context)
}