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
    suspend fun insertAll(vararg images: FavoriteImageEntity)

    @Delete
    suspend fun delete(image: FavoriteImageEntity)

    @Query("SELECT * FROM favorite_images")
    fun getAll(): Flow<List<FavoriteImageEntity>>

    @Query("DELETE FROM favorite_images WHERE id = :imageId")
    suspend fun deleteById(imageId: String)

    @Query("SELECT * FROM favorite_images WHERE id = :imageId")
    suspend fun getById(imageId: String): FavoriteImageEntity?

}