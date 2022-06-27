package com.quangln2.mydownloadmanager.util

import com.quangln2.mydownloadmanager.R

class UIComponentUtil {
    companion object {
        fun defineIcon(type: String): Int {
            return when (type) {
                "All" -> R.drawable.ic_baseline_all
                "Documents" -> R.drawable.ic_article
                "Compressed" -> R.drawable.ic_folder_zip
                "Packages" -> R.drawable.ic_packages
                "Music"-> R.drawable.ic_music
                "Video"-> R.drawable.ic_video
                else -> R.drawable.ic_others
            }
        }
        fun defineTypeOfCategoriesBasedOnFileName(contentType: String): String {
            if(contentType.contains("text") || contentType == "application/pdf"){
                return "Documents"
            }
            if(contentType.contains("zip") || contentType.contains("rar")){
                return "Compressed"
            }
            if(contentType.contains("application")){
                return "Packages"
            }
            if(contentType.contains("audio")){
                return "Music"
            }
            if(contentType.contains("video")){
                return "Video"
            }
            return "Others"
        }
    }
}