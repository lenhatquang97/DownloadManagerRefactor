package com.quangln2.mydownloadmanager.data.database

import androidx.room.*
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_list")
    fun getAll(): Flow<List<StrucDownFile>>

    @Query("SELECT * FROM download_list WHERE kind_of = :kind_of")
    fun getById(kind_of: String): Flow<List<StrucDownFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(strucDownFile: StrucDownFile)

    @Delete
    fun delete(strucDownFile: StrucDownFile)

}