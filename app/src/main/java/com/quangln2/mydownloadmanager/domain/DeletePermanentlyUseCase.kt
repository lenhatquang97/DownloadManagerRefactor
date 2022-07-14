package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class DeletePermanentlyUseCase(private val downloadRepository: DefaultDownloadRepository) {
    suspend operator fun invoke(file: StrucDownFile, context: Context) =
        downloadRepository.deletePermanently(file, context)
}