package com.quangln2.mydownloadmanager.data.database

import androidx.room.*
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_list")
    fun getAll(): Flow<List<StrucDownFile>>

    @Insert
    fun insert(file: StrucDownFile)

    @Update
    fun update(file: StrucDownFile)

    @Delete
    fun delete(file: StrucDownFile)

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM download_list WHERE download_link = :downloadLink AND (download_state = \"Downloading\" OR download_state = \"Completed\")) THEN 1 ELSE 0 END")
    fun doesDownloadLinkExist(downloadLink: String): Int


}