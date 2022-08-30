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
        return if(str.isEmpty()) {
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
        if(str != null && str.isNotEmpty()){
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
        if(str != null && str.isNotEmpty()){
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

    //    @Query(
//        "SELECT CASE WHEN EXISTS " +
//                "(SELECT * FROM download_list " +
//                "WHERE download_link = :downloadLink AND (download_state = \"Downloading\" OR download_state = \"Completed\")) " +
//                "THEN 1 ELSE 0 END"
//    )
    fun doesDownloadLinkExist(downloadLink: String): Int {
        val pref = PrefSingleton.instance
        val str = pref?.getString(DOWNLOAD_LIST)
        if(str.isNullOrEmpty()) {
            return 0
        }
        val list = Converters.convertDownloadList(str)
        for (i in list.indices) {
            if (list[i].downloadLink == downloadLink && (list[i].downloadState == DownloadStatusState.DOWNLOADING || list[i].downloadState == DownloadStatusState.COMPLETED)) {
                return 1
            }
        }
        return 0
    }

}