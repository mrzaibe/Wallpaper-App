package com.example.mvvmtask.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mvvmtask.data.database.dao.SavedPhotosDao
import com.example.mvvmtask.data.database.entities.SavedPhotosEntity


@Database(
    entities = [
        SavedPhotosEntity::class
    ],
    version = 1
)
abstract class WallPaperDatabase : RoomDatabase() {
    abstract fun savedPhotosDao(): SavedPhotosDao
}