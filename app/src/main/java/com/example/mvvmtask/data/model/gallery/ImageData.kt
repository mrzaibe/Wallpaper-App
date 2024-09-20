package com.example.mvvmtask.data.model.gallery

import com.example.mvvmtask.data.database.entities.ImageEntity

data class ImageData(
    override val id: Long,
    override val title: String = "",
    override val imagePath: String
) : ImageEntity