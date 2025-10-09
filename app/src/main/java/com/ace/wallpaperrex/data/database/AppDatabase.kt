package com.ace.wallpaperrex.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ace.wallpaperrex.data.daos.FavoriteImageDao
import com.ace.wallpaperrex.data.daos.SearchHistoryDao
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import com.ace.wallpaperrex.data.entities.SearchHistoryItem

@Database(
    entities = [FavoriteImageEntity::class, SearchHistoryItem::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteImageDao(): FavoriteImageDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}