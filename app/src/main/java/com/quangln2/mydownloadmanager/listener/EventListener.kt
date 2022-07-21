package com.quangln2.mydownloadmanager.listener

import android.content.Context
import android.view.MenuItem
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

interface EventListener {
    fun onHandleDelete(
        menuItem: MenuItem,
        binding: DownloadItemBinding,
        item: StrucDownFile,
        context: Context
    ): Boolean

    fun onDownloadSuccess(binding: DownloadItemBinding, item: StrucDownFile, context: Context)
    fun onPause(item: StrucDownFile, binding:DownloadItemBinding)
    fun onResume(item: StrucDownFile, binding:DownloadItemBinding)
    fun onOpen(item: StrucDownFile, binding:DownloadItemBinding)
    fun onRetry(item: StrucDownFile, binding:DownloadItemBinding)
    fun onStop(item: StrucDownFile, binding:DownloadItemBinding)
    fun onUpdateToDatabase(item: StrucDownFile)
}