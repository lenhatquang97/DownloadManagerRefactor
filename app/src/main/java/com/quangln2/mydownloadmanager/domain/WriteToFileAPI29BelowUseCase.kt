package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class WriteToFileAPI29BelowUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(StructureDownFile: StructureDownFile) =
        downloadRepository.writeToFileAPI29Below(StructureDownFile)

}