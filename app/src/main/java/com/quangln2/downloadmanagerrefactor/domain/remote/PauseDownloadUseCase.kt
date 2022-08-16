package com.quangln2.downloadmanagerrefactor.domain.remote

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class PauseDownloadUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile) =
        downloadRepository.pauseDownload(file)
}