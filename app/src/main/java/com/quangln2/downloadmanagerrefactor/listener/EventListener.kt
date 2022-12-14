package com.quangln2.downloadmanagerrefactor.listener

import android.content.Context
import android.view.MenuItem
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.databinding.DownloadItemBinding

interface EventListener {
    fun onHandleDelete(
        menuItem: MenuItem,
        binding: DownloadItemBinding,
        item: StructureDownFile,
        context: Context
    ): Boolean

    fun onDownloadSuccess(binding: DownloadItemBinding, item: StructureDownFile, context: Context)
    fun onPause(item: StructureDownFile)
    fun onResume(item: StructureDownFile)
    fun onOpen(item: StructureDownFile)
    fun onRetry(item: StructureDownFile)
    fun onStop(item: StructureDownFile, binding: DownloadItemBinding, context: Context)
    fun onUpdateToDatabase(item: StructureDownFile)
}