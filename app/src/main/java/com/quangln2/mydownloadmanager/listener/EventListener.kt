package com.quangln2.mydownloadmanager.listener

import android.content.Context
import android.view.MenuItem
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

interface EventListener {
    fun onHandleDelete(
        menuItem: MenuItem,
        binding: DownloadItemBinding,
        item: StructureDownFile,
        context: Context
    ): Boolean

    fun onDownloadSuccess(binding: DownloadItemBinding, item: StructureDownFile, context: Context)
    fun onPause(item: StructureDownFile, binding: DownloadItemBinding)
    fun onResume(item: StructureDownFile, binding: DownloadItemBinding)
    fun onOpen(item: StructureDownFile, binding: DownloadItemBinding)
    fun onRetry(item: StructureDownFile, binding: DownloadItemBinding)
    fun onStop(item: StructureDownFile, binding: DownloadItemBinding)
    fun onUpdateToDatabase(item: StructureDownFile)
}