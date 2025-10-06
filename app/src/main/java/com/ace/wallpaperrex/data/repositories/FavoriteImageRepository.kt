package com.ace.wallpaperrex.data.repositories

import com.ace.wallpaperrex.data.daos.FavoriteImageDao
import com.ace.wallpaperrex.data.entities.FavoriteImageEntity
import kotlinx.coroutines.flow.Flow

class FavoriteImageRepository(private val dao: FavoriteImageDao) {
    val allFavorites: Flow<List<FavoriteImageEntity>> = dao.getAll()

    suspend fun addFavorite(entity: FavoriteImageEntity) {
        dao.insertAll(entity)
    }

    suspend fun removeFavorite(entity: FavoriteImageEntity) {
        dao.deleteById(entity.id)
    }

    suspend fun addMultipleFavorites(entities: List<FavoriteImageEntity>) {
        dao.insertAll(*entities.toTypedArray())
    }

    suspend fun getById(imageId: String): FavoriteImageEntity? {
        return dao.getById(imageId)
    }

}