package com.quangln2.mydownloadmanager.listener

import android.content.Context
import android.view.MenuItem
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

interface EventListener {
    fun onFirstSetup(item: StrucDownFile)
    fun onOpenNotification(item: StrucDownFile, content: String, progress: Int)
    fun onHandleDelete(menuItem: MenuItem, binding: DownloadItemBinding, item: StrucDownFile, context: Context): Boolean
}