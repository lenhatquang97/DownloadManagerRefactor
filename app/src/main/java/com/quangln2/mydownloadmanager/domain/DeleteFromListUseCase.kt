package com.quangln2.mydownloadmanager.domain

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class DeleteFromListUseCase(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(file: StrucDownFile) = downloadRepository.deleteFromList(file)
}