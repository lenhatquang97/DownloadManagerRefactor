package com.quangln2.mydownloadmanager.data.database

import androidx.room.*
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_list")
    fun getAll(): Flow<List<StrucDownFile>>

    @Insert
    fun insert(strucDownFile: StrucDownFile)

    @Update
    fun update(strucDownFile: StrucDownFile)

    @Delete
    fun delete(strucDownFile: StrucDownFile)

}