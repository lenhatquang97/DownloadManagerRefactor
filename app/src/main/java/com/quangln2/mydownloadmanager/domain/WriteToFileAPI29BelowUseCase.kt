package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DownloadRepository

class WriteToFileAPI29BelowUseCase(private val downloadRepository: DownloadRepository){
    operator fun invoke(strucDownFile: StrucDownFile): String = downloadRepository.writeToFileAPI29Below (strucDownFile)

}