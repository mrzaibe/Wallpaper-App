package com.example.mvvmtask.data.repositoryImp

import com.example.mvvmtask.data.database.dao.SavedPhotosDao
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity

class SavedPhotosRepository(private var savedPhotosDao: SavedPhotosDao) {

    suspend fun insertPhotos(savedPhotosEntity: SavedPhotosEntity) {
        savedPhotosDao.insertPhotos(savedPhotosEntity)
    }

    fun getAllSavedPhotos()=savedPhotosDao.getAllSavedPhotos()
}