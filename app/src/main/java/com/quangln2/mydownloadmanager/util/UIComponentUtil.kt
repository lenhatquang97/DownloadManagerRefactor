package com.quangln2.mydownloadmanager.util

import androidx.documentfile.provider.DocumentFile
import com.quangln2.mydownloadmanager.R

class UIComponentUtil {
    companion object {
        fun defineIcon(type: String): Int {
            return when (type) {
                "All" -> R.drawable.ic_baseline_all
                "Documents" -> R.drawable.ic_article
                "Compressed" -> R.drawable.ic_folder_zip
                "Packages" -> R.drawable.ic_packages
                "Music" -> R.drawable.ic_music
                "Video" -> R.drawable.ic_video
                else -> R.drawable.ic_others
            }
        }

        fun defineTypeOfCategoriesBasedOnFileName(contentType: String): String {
            if (contentType.contains("text") || contentType == "application/pdf") {
                return "Documents"
            }
            if (contentType.contains("zip") || contentType.contains("rar")) {
                return "Compressed"
            }
            if (contentType.contains("application")) {
                return "Packages"
            }
            if (contentType.contains("audio")) {
                return "Music"
            }
            if (contentType.contains("video")) {
                return "Video"
            }
            return "Others"
        }

        fun getRealPath(treeUri: DocumentFile?): String {
            if (treeUri == null) return ""
            val path1: String = treeUri.uri.path!!
            if (path1.startsWith("/tree/")) {
                val path2 = path1.removeRange(0, "/tree/".length)
                if (path2.startsWith("primary:")) {
                    val primary = path2.removeRange(0, "primary:".length)
                    if (primary.contains(':')) {
                        val storeName = "/storage/emulated/0/"
                        val last = path2.split(':').last()
                        return storeName + last
                    }
                } else {
                    if (path2.contains(':')) {
                        val path3 = path2.split(':').first()
                        val last = path2.split(':').last()
                        return "/$path3/$last"
                    }
                }
            }
            return path1
        }

    }
}