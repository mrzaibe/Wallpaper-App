package com.example.mvvmtask.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity


@Dao
interface SavedPhotosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(savedPhotosEntity:SavedPhotosEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplePhotos(savedPhotosEntity:List<SavedPhotosEntity>)

    @Query("Select * from saved_photos")
    fun getAllSavedPhotos(): LiveData<List<SavedPhotosEntity>>
}