package com.quangln2.mydownloadmanager.domain

import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository

class WriteToFileAPI29BelowUseCase(private val downloadRepository: DefaultDownloadRepository){
    operator fun invoke(strucDownFile: StrucDownFile) = downloadRepository.writeToFileAPI29Below (strucDownFile)

}