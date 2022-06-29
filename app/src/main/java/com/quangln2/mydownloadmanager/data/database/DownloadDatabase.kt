package com.quangln2.mydownloadmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quangln2.mydownloadmanager.data.converter.Converters
import com.quangln2.mydownloadmanager.data.model.StrucDownFile

@Database(entities = [StrucDownFile::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DownloadDatabase: RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getDatabase(context: Context): DownloadDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if(INSTANCE == null){
                synchronized(this){
                    INSTANCE = Room.databaseBuilder(context,
                        DownloadDatabase::class.java, "download_database.db")
                        .build()
                }
            }
            return INSTANCE!!
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    DownloadDatabase::class.java,
//                    "download_database"
//                ).build()
//                INSTANCE = instance
//                // return instance
//                instance
//            }
        }
    }
}