package com.quangln2.mydownloadmanager.util

import android.content.Context
import android.widget.ProgressBar
import com.quangln2.mydownloadmanager.R

class UIComponentUtil {
    companion object {
        fun defineIcon(type: String): Int {
            when (type) {
                "All" -> return R.drawable.ic_baseline_all
                "Documents" -> return R.drawable.ic_article
                "Compressed" -> return R.drawable.ic_folder_zip
                "Packages" -> return R.drawable.ic_packages
                "Music"-> return R.drawable.ic_music
                "Video"-> return R.drawable.ic_video
                else -> return R.drawable.ic_others
            }
        }
    }
}