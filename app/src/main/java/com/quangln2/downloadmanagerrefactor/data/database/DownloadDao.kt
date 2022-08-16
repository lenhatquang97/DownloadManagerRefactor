package com.quangln2.downloadmanagerrefactor.data.database

import androidx.room.*
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_list")
    fun getAll(): Flow<List<StructureDownFile>>

    @Insert
    fun insert(file: StructureDownFile)

    @Update
    fun update(file: StructureDownFile)

    @Delete
    fun delete(file: StructureDownFile)

    @Query(
        "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM download_list " +
                "WHERE download_link = :downloadLink AND (download_state = \"Downloading\" OR download_state = \"Completed\")) " +
                "THEN 1 ELSE 0 END"
    )
    fun doesDownloadLinkExist(downloadLink: String): Int


}