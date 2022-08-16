package com.quangln2.downloadmanagerrefactor.domain.local

import android.content.Context
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository

class VibratePhoneUseCase(private val downloadRepository: DefaultDownloadRepository) {
    operator fun invoke(context: Context) {
        downloadRepository.vibratePhone(context)
    }
}