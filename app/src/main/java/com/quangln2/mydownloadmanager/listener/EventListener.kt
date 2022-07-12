package com.quangln2.mydownloadmanager.listener

import android.content.Context
import android.view.MenuItem
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

interface EventListener {
    fun onHandleDelete(menuItem: MenuItem, binding: DownloadItemBinding, item: StrucDownFile, context: Context): Boolean
    fun onDownloadSuccess(binding: DownloadItemBinding, item: StrucDownFile, context: Context)
    fun onPause()
    fun onResume()
    fun onOpen()
    fun onRetry()
    fun onUpdateToDatabase()
}