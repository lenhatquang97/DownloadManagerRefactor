package com.quangln2.mydownloadmanager.listener

import android.content.Context
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

interface EventListener {
    fun onFirstSetup(item: StrucDownFile)
    fun onOpenNotification(item: StrucDownFile, content: String, progress: Int)
    fun downloadAFileWithProgressBar(binding: DownloadItemBinding, item: StrucDownFile, context: Context)
}