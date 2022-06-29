package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class GetBytesFromExistingFileUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(file: StrucDownFile, context: Context) =
        downloadRepository.getBytesFromExistingFile(file, context)
}