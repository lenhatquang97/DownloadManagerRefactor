package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class IsFileExistingUseCase(private val repository: DownloadRepository) {
    operator fun invoke(download: StrucDownFile, context: Context) = repository.isFileExisting(download, context)
}