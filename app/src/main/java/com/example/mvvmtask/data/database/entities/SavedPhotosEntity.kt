package com.example.mvvmtask.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "saved_photos", indices = [Index(value = ["photoId"], unique = true)])
data class SavedPhotosEntity (
    @PrimaryKey(autoGenerate = true)
    val photoId: Long? = null,
    override val imagePath: String = "",
    override val title: String = ""
) : Serializable, ImageEntity {
    override val id: Long get() = photoId ?: 0L
}