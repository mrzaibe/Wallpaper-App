package com.example.mvvmtask.data.model

data class WallPaperPhotos(
    val alt: String,
    val avg_color: String?,
    val height: Int,
    val id: Long,
    val liked: Boolean,
    val photographer: String,
    val photographer_id: Int,
    val photographer_url: String,
    val src: Src,
    val url: String,
    val width: Int
)