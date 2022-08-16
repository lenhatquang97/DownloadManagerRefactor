package com.quangln2.downloadmanagerrefactor.domain.remote

import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class FetchDownloadInfoUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(file: StructureDownFile): StructureDownFile =
        downloadRepository.fetchDownloadInfo(file)

}