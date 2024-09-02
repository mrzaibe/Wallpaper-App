package com.example.mvvmtask.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "saved_photos", indices = [Index(value = ["photoId"], unique = true)])
data class SavedPhotosEntity (
    @PrimaryKey(autoGenerate = false)
    val photoId: Long,
    val imagePath: String? = null,
    val title: String? = null
):Serializable