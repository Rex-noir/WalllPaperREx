package com.ace.wallpaperrex.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg images: FavoriteImageEntity)

    @Delete
    fun delete(image: FavoriteImageEntity)

    @Query("SELECT * FROM favorite_images")
    fun getAll(): Flow<List<FavoriteImageEntity>>

}