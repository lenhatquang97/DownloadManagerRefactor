package com.quangln2.downloadmanagerrefactor.domain.local

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class WriteToFileAPI29BelowUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(StructureDownFile: StructureDownFile) =
        downloadRepository.writeToFileAPI29Below(StructureDownFile)

}