package com.quangln2.downloadmanagerrefactor.data.database

import com.quangln2.downloadmanagerrefactor.data.converter.Converters
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState

class DownloadDao {
    companion object {
        const val DOWNLOAD_LIST = "download_list"
    }

    fun getAll(): List<StructureDownFile> {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST) ?: ""
        return if (str.isEmpty()) {
            emptyList()
        } else {
            Converters.convertDownloadList(str)
        }
    }

    fun insert(file: StructureDownFile) {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST)
        val list = if (str.isNullOrEmpty()) mutableListOf() else Converters.convertDownloadList(str)
        list.add(file)
        pref?.writeString(DOWNLOAD_LIST, Converters.convertDownloadList(list))
    }

    fun update(file: StructureDownFile) {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST)
        if (str != null && str.isNotEmpty()) {
            val list = Converters.convertDownloadList(str)
            for (i in list.indices) {
                if (list[i].id == file.id) {
                    list[i] = file.copy()
                    break
                }
            }
            pref.writeString(DOWNLOAD_LIST, Converters.convertDownloadList(list))
        }

    }

    fun delete(file: StructureDownFile) {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST)
        if (str != null && str.isNotEmpty()) {
            val list = Converters.convertDownloadList(str)
            for (i in list.indices) {
                if (list[i].id == file.id) {
                    list.removeAt(i)
                    break
                }
            }
            pref.writeString(DOWNLOAD_LIST, Converters.convertDownloadList(list))
        }

    }

    fun doesDownloadLinkExist(downloadLink: String): Boolean {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST)
        if (str.isNullOrEmpty()) {
            return false
        }
        val list = Converters.convertDownloadList(str)
        for (i in list.indices) {
            if (list[i].downloadLink == downloadLink && (list[i].downloadState == DownloadStatusState.DOWNLOADING || list[i].downloadState == DownloadStatusState.COMPLETED)) {
                return true
            }
        }
        return false
    }

}