package com.quangln2.downloadmanagerrefactor.util

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.listener.OnAcceptPress

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

        fun showDownloadDialogAgain(
            context: Context,
            file: StructureDownFile,
            onAcceptPress: OnAcceptPress
        ) {
            val builder =
                MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                    .setTitle(file.fileName)
                    .setMessage(ConstantClass.DOWNLOAD_AGAIN_MESSAGE)
                    .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                        onAcceptPress.onAcceptPress()
                        a.dismiss()
                    }
                    .setNegativeButton(ConstantClass.NEGATIVE_BUTTON) { a, _ ->
                        onAcceptPress.onNegativePress()
                        a.dismiss()
                    }
            builder.show()
        }

        fun showDownloadAlertDialog(
            context: Context,
            file: StructureDownFile,
            onAcceptPress: OnAcceptPress
        ) {
            val builder =
                MaterialAlertDialogBuilder(context, R.style.AlertDialogShow)
                    .setTitle(file.fileName)
                    .setIcon(R.drawable.ic_baseline_arrow_downward_24)
                    .setMessage(ConstantClass.DOWNLOAD_MESSAGE + file.convertToSizeUnit())
                    .setPositiveButton(ConstantClass.POSITIVE_BUTTON) { a, _ ->
                        onAcceptPress.onAcceptPress()
                        a.dismiss()
                    }
                    .setNegativeButton(ConstantClass.NEGATIVE_BUTTON) { a, _ ->
                        onAcceptPress.onNegativePress()
                        a.dismiss()
                    }
            builder.show()
        }

    }
}